package edu.iis.mto.oven;

import static edu.iis.mto.oven.Oven.HEAT_UP_AND_FINISH_SETTING_TIME;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class OvenTest {

    @Mock
    private HeatingModule heatingModule;

    @Mock
    private Fan fan;

    Oven oven;
    ProgramStage sampleTermoCirculationStage = ProgramStage.builder().withStageTime(10).withHeat(HeatType.THERMO_CIRCULATION).withTargetTemp(150).build();
    ProgramStage sampleGrillStage = ProgramStage.builder().withStageTime(10).withHeat(HeatType.GRILL).withTargetTemp(150).build();
    ProgramStage sampleHeaterStage = ProgramStage.builder().withStageTime(10).withHeat(HeatType.HEATER).withTargetTemp(150).build();


    @BeforeEach
    public void setUp(){
        oven = new Oven(heatingModule, fan);
    }

    @Test
    void shouldInvokeHeatingModuleHeaterWithExpectedSettingsWhenInitializing() {
        BakingProgram sampleBakingProgram = BakingProgram.builder().withInitialTemp(50).withStages(Collections.emptyList()).build();
        HeatingSettings expectedSettings = HeatingSettings.builder()
                .withTargetTemp(sampleBakingProgram.getInitialTemp())
                .withTimeInMinutes(HEAT_UP_AND_FINISH_SETTING_TIME)
                .build();
        doNothing().when(heatingModule).heater(expectedSettings);

        oven.start(sampleBakingProgram);

        verify(heatingModule).heater(expectedSettings);
    }

    @Test
    void shouldNotInvokeHeatingModuleHeaterWhenInitialTemperatureIsZero() {
        BakingProgram sampleBakingProgram = BakingProgram.builder().withInitialTemp(0).withStages(Collections.emptyList()).build();

        oven.start(sampleBakingProgram);

        verify(heatingModule, never()).heater(any());
    }

    @Test
    void shouldRunTermoCirculationStageInCorrectOrder() throws HeatingException {
        BakingProgram sampleBakingProgram = BakingProgram.builder().withInitialTemp(0).withStages(List.of(sampleTermoCirculationStage)).build();
        doNothing().when(fan).on();
        doNothing().when(heatingModule).termalCircuit(any());
        doNothing().when(fan).off();

        oven.start(sampleBakingProgram);

        InOrder inOrder = inOrder(heatingModule, fan);
        inOrder.verify(fan).on();
        inOrder.verify(heatingModule).termalCircuit(any());
        inOrder.verify(fan).off();

    }

    @Test
    void shouldInvokeHeatingProgramGrillWithCorrectSettingsWhenHitTypeIsGrill() throws HeatingException {

        BakingProgram sampleBakingProgram = BakingProgram.builder().withInitialTemp(0).withStages(List.of(sampleGrillStage)).build();
        when(fan.isOn()).thenReturn(false);
        HeatingSettings heatingSettings = HeatingSettings.builder()
                .withTargetTemp(sampleGrillStage.getTargetTemp())
                .withTimeInMinutes(sampleGrillStage.getStageTime())
                .build();
        doNothing().when(heatingModule).grill(heatingSettings);

        oven.start(sampleBakingProgram);
        verify(heatingModule).grill(heatingSettings);

    }

    @Test
    void shouldInvokeHeatingProgramHeaterWithCorrectSettingsWhenHitTypeIsHeat() {

        BakingProgram sampleBakingProgram = BakingProgram.builder().withInitialTemp(0).withStages(List.of(sampleHeaterStage)).build();
        when(fan.isOn()).thenReturn(false);
        HeatingSettings heatingSettings = HeatingSettings.builder()
                .withTargetTemp(sampleHeaterStage.getTargetTemp())
                .withTimeInMinutes(sampleHeaterStage.getStageTime())
                .build();
        doNothing().when(heatingModule).heater(heatingSettings);

        oven.start(sampleBakingProgram);
        verify(heatingModule).heater(heatingSettings);

    }

    @Test
    void shouldThrowOvenExceptionWhenInvokeTermalCircuit() throws HeatingException {
        BakingProgram sampleBakingProgram = BakingProgram.builder().withInitialTemp(0).withStages(List.of(sampleTermoCirculationStage)).build();

        doNothing().when(fan).on();
        doThrow(new OvenException(new HeatingException())).when(heatingModule).termalCircuit(any());

        assertThrows(OvenException.class, () -> oven.start(sampleBakingProgram));
    }




}
