package ex0;

import ex0.Building;
import ex0.Elevator;
import ex0.algo.ElevatorAlgo;
import ex0.algo.SmartElevatorAlgo;
import ex0.simulator.Call_A;
import ex0.simulator.ElevetorCallList;
import ex0.simulator.Simulator_A;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class AlgoTester {

    private ElevatorAlgo ex0_alg;
    private Call_A[] calls;

    public AlgoTester()
    {

    }

    public void setState(int state)
    {
        Simulator_A.initData(state, null);
        ex0_alg = new SmartElevatorAlgo(Simulator_A.getBuilding());
        Simulator_A.initAlgo(ex0_alg);
        calls = new Call_A[] { new Call_A(1,0,60),
                new Call_A(3,54,59),
                new Call_A(5,40,57),
                new Call_A(7,12,63),
                new Call_A(9,64,80),
                new Call_A(9,64,80),
                new Call_A(11,80,0),
                new Call_A(13,19,-1),
                new Call_A(15,82,100)
        };
    }

    @Test
    public void allocateAnElevator()
    {
        setState(9);

        assertEquals(3 ,ex0_alg.allocateAnElevator(calls[0]));
        assertEquals(1 ,ex0_alg.allocateAnElevator(calls[1]));
        assertEquals(0 ,ex0_alg.allocateAnElevator(calls[2]));
        assertEquals(9 ,ex0_alg.allocateAnElevator(calls[3]));
        assertEquals(8 ,ex0_alg.allocateAnElevator(calls[4]));
        assertEquals(7 ,ex0_alg.allocateAnElevator(calls[5]));
        assertEquals(6 ,ex0_alg.allocateAnElevator(calls[6]));
        assertEquals(5 ,ex0_alg.allocateAnElevator(calls[7]));
        assertEquals(3 ,ex0_alg.allocateAnElevator(calls[8]));

    }

    @Test
    public void cmdElevator()
    {
        setState(8);
        Building build = Simulator_A.getBuilding();
        int[] elevators  = {3, 1, 0, 9, 8, 7, 6, 5, 3};

        for (int i = 0; i < calls.length; i++)
        {
            ex0_alg.allocateAnElevator(calls[i]);
        }

        for (int j = 0; j < 5; j++)
        {
            for (int i = 0; i < elevators.length; i++)
            {
                ex0_alg.cmdElevator(elevators[i]);
            }
        }

    }
}