package edu.iis.mto.oven;

import static edu.iis.mto.oven.Oven.HEAT_UP_AND_FINISH_SETTING_TIME;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    ProgramStage sampleProgramStage = ProgramStage.builder().withStageTime(10).withHeat(HeatType.GRILL).withTargetTemp(150).build();
    BakingProgram sampleBakingProgram = BakingProgram.builder().withInitialTemp(50).withStages(List.of(sampleProgramStage)).build();

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


}
