package ex0.algo;

import ex0.Building;
import ex0.CallForElevator;
import ex0.Elevator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

import static java.lang.Thread.sleep;

public class SmartElevatorAlgo implements ElevatorAlgo
{
    public static final int UP=1, DOWN=-1;
    private int[] _direction;                           // the array that contains the elevators directions
    private Building _building;                         // curr building
    private TreeSet<Integer>[] _stopLists;              // list for each elevator that contains the stops tree set
    private int _maxStops;                              // the max stops that the elevator can have
    private HashSet<Integer> _hashUp;                   // Hash set of all the current up elevators
    private HashSet<Integer> _hashDown;                 // Hash set of all the current down elevators
    private HashSet<Integer> _hashLevel;                // Hash set of all the current static elevators
    private int _lenElv;                                // the amount of our elevators
    private ArrayList<CallForElevator>[] _callsBuffer;  // the calls that didn't had an answer


    /**
     * c'tor
     * @param b
     */
    public SmartElevatorAlgo(Building b)
    {
        _building = b;
        _lenElv = _building.numberOfElevetors();
        _stopLists = new TreeSet[_lenElv];
        _direction = new int[_lenElv];
        _hashUp = new HashSet<>();
        _hashDown = new HashSet<>();
        _hashLevel = new HashSet<>();
        _callsBuffer = new ArrayList[_lenElv];

        for (int i = 0; i < _lenElv; i++)
        {
            _stopLists[i] = new TreeSet<>();
            _direction[i] = 0;
            _hashLevel.add(i);
            _callsBuffer[i] = new ArrayList<>();

        }
        _maxStops = ((_building.maxFloor() - _building.minFloor()) / _lenElv);
        //_maxStops = Integer.MAX_VALUE;
    }
    @Override
    public Building getBuilding()
    {
        return _building;
    }

    @Override
    public String algoName()
    {
        return "Batel's & Gilad's crazy Smart Algorithm";
    }

    /**
     * this function allocates the call to elevator for the best elevator
     * @param c the call for elevator (src, dest)
     * @return
     */
    @Override
    public int allocateAnElevator(CallForElevator c)
    {

        int ans = 0;

        if (_building.numberOfElevetors() > 1)
        {
            if (c.getSrc() < c.getDest())           // UP call
            {
                ans = getBestIndexUp(c);            // get the best index for up elevator
                _direction[ans] = UP;
                _hashLevel.remove(ans);             // the elevator is up for hash
                _hashUp.add(ans);
            }
            else if (c.getSrc() > c.getDest())      // Down call
            {
                ans = getBestIndexDown(c);          // get the best index for down elevator
                _direction[ans] = DOWN;
                _hashLevel.remove(ans);             // the elevator is down for hash
                _hashDown.add(ans);
            }
        }
        else if (_building.numberOfElevetors() == 1 && _direction[ans] == 0)        // only one elevator
        {
            if (c.getSrc() < c.getDest())           // UP
            {
                _direction[ans] = UP;
            }
            else if (c.getSrc() > c.getDest())      // if
            {
                _direction[ans] = DOWN;
            }
        }
        setStops(ans, c);
        return ans;
    }

    /**
     *
     * @param elev the current Elevator index on which the operation is performs.
     */
    @Override
    public void cmdElevator(int elev)
    {
        Elevator curr = this.getBuilding().getElevetor(elev);

        if (elev != curr.getID())       // if not matches then return
            return;

        if (curr.getState() == Elevator.LEVEL &&_stopLists[elev].size() > 0)        // if the elevator is resting and has stops
        {
            if (_direction[elev] == UP || _direction[elev] == 0)
            {
                curr.goTo(_stopLists[elev].pollFirst());        // get the first stop from bottom and set her dir to the hashes
                _direction[elev] = UP;
                _hashLevel.remove(elev);
                _hashUp.add(elev);
            }
            else if (_direction[elev] == DOWN)
            {
                curr.goTo(_stopLists[elev].pollLast());       // get the first stop from top and set her dir to the hashes
                _direction[elev] = DOWN;
                _hashLevel.remove(elev);
                _hashDown.add(elev);
            }
        }

        isFinishedElev(elev);       // check allways if adaptation about resting is needed
        emptyBuffer(elev);          // if there are some calls that has not reported then add them to deal

    }

    /**
     * deciding which index of elevator is the best to use  - for up call
     * @param c - the call for the current elevator
     * @return
     */
    private int getBestIndexUp(CallForElevator c)
    {
        if (_lenElv == 1)       //if only one - then there is only one option
            return 0;

        int index = getRandUp();        // get the random index from the elevtors who rests or up - to be more fliud
        int original = index;

        for (int i = 0; i < _lenElv; i++)
        {
            if (i == original)      // if its the original choise  - jump
                continue;

            Elevator curr = getBuilding().getElevetor(i);

            if (_direction[i] == Elevator.DOWN || (_hashUp.size() == _lenElv - 1)) // if down or if there are (len -1) elevators not moving dont use it
                continue;

            if (curr.getMinFloor() <= c.getSrc() && c.getDest() <= curr.getMaxFloor() && _stopLists[i].size() <= _maxStops)     // check if in boundries and not over the max stop num
            {
                // checks if the src flr is over our position
                if (_stopLists[i].size() == 0 || ((curr.getPos() <= c.getSrc())))
                {
                    // checks the best time, if current is lower than it is th best for this round
                    if (getCalcTimeUp(curr, c.getSrc(), c.getDest()) <= getCalcTimeUp(getBuilding().getElevetor(index), c.getSrc(), c.getDest()))
                    {
                        index = i;
                    }
                }
            }
        }
        return index;
    }

    /**
     * deciding wich index of elevator is the best to use  - for down call
     * @param c
     * @return
     */
    private int getBestIndexDown(CallForElevator c)
    {
        int len = getBuilding().numberOfElevetors();
        if (len == 1)
            return 0;

        int index = getRandDown();
        int original = index;

        for (int i = 0; i < len; i++)
        {
            if (i == original)
                continue;

            Elevator curr = getBuilding().getElevetor(i);

            if (_direction[i] == UP || (_hashDown.size() == _lenElv - 1 && _direction[i] == 0)) // if down or if there are (len -1) elevators not moving dont use it
                continue;

            if (curr.getMinFloor() <= c.getSrc() && c.getDest() <= curr.getMaxFloor() && _stopLists[i].size() <= _maxStops)  // check if in boundries and not over the max stop num
            {
                if (_stopLists[i].size() == 0 || ((c.getSrc() <= curr.getPos())))
                {
                    // checks the best time, if current is lower than it is th best for this round
                    if (getCalcTimeDown(curr, c.getSrc(), c.getDest()) <= getCalcTimeDown(getBuilding().getElevetor(index), c.getSrc(), c.getDest()))
                    {
                        index = i;
                    }
                }
            }
        }

        return index;
    }

    /**
     * calculates the time to reach the dest floor
     * @param curr
     * @param src
     * @param floor
     * @return
     */
    private double getCalcTimeUp(Elevator curr, int src, int floor)
    {
        int stopsNum = howManyStopsUp(curr.getID(), floor);
        double time = stopsNum * (curr.getTimeForOpen() + curr.getTimeForClose() + curr.getStartTime() + curr.getStopTime()); // how many times we have to stop and the time it takes for moving the elevator
        time += (Math.abs(floor - src) + Math.abs(curr.getPos() - src)) / curr.getSpeed();  // check - go to grc from pos and then so to dest - (floors / (flr/sec)) = sec
        return time;
    }

    private double getCalcTimeDown(Elevator curr, int src, int floor)
    {
        int stopsNum = howManyStopsDown(curr.getID(), floor);
        double time = stopsNum * (curr.getTimeForOpen() + curr.getTimeForClose() + curr.getStartTime() + curr.getStopTime());
        time += (Math.abs(floor - src) + Math.abs(curr.getPos() - src)) / curr.getSpeed(); // check - go to grc from pos and then so to dest - (floors / (flr/sec)) = sec

        return time;
    }

    /**
     * checks how many stops there are below the wanted floor
     * @param index
     * @param floor
     * @return
     */
    private int howManyStopsUp(int index, int floor)
    {
        return _stopLists[index].headSet(floor).size();
    }

    /**
     * checks how many stops there are over the wanted floor
     * @param index
     * @param floor
     * @return
     */
    private int howManyStopsDown(int index, int floor)
    {
        return _stopLists[index].tailSet(floor).size();
    }


    /**
     * set the stop the the stop list - if not suited than save on buffer
     * @param elev
     * @param c
     * @return
     */
    private boolean setStops(int elev, CallForElevator c)
    {
        if (_stopLists[elev].size() > 0 || _callsBuffer[elev].size() > 0)
        {
            // if the call is up  - if not the same direction or the src is below our position then cannot be treated at the moment
            //then save at the buffer
            if (c.getSrc() < c.getDest() &&
                    (_direction[elev] == DOWN || (_stopLists[elev].size() > 0 && c.getSrc() < _stopLists[elev].first() && c.getSrc() < _building.getElevetor(elev).getPos())))
            {
                _callsBuffer[elev].add(c);
                return false;
            }

            // if the call is down  - if not the same direction or the src is over our position then cannot be treated at the moment
            //then save at the buffer
            else if (c.getSrc() > c.getDest() && (_direction[elev] == UP || (_stopLists[elev].size() > 0 && c.getSrc() > _stopLists[elev].last() && c.getSrc() > _building.getElevetor(elev).getPos())))
            {
                _callsBuffer[elev].add(c);
                return false;
            }
        }

        _stopLists[elev].add(c.getSrc());       // if fine then set the stops
        _stopLists[elev].add(c.getDest());
        return true;

    }

    // get random number - [min, max)
    private static int rand(int min, int max)
    {
        if(max<min) {throw new RuntimeException("ERR: wrong values for range max should be >= min");}
        int ans = min;
        double dx = max-min;
        double r = Math.random()*dx;
        ans = ans + (int)(r);
        return ans;
    }

    /**
     * checks and sets if elevator have to be in rest mode
     * @param elev
     * @return false if not in rest
     */
    private boolean isFinishedElev(int elev)
    {
        if (_stopLists[elev].size() == 0 && _callsBuffer[elev].size() == 0 && !_hashLevel.contains(elev))   // if dont have any work to do
        {
            _direction[elev] = 0;
            if (!_hashUp.remove(elev))
            {
                _hashDown.remove(elev);
            }
            _hashLevel.add(elev);
            return true;
        }
        return false;
    }

    /**
     * get the random value of the elevator that going up or resting
     * @return
     */
    private int getRandUp()
    {
        int ind = 0;
        Iterator iterator = _hashLevel.iterator();
        if (_hashUp.size() == 0)                    // if there is no up elevator there take from the resting one's
        {
            ind = rand(0, _hashLevel.size());   // generate random nuber on the len of the resting elev's

            for (int i = 0; i < ind; i++)
            {
                iterator.next();
            }
            return (int) iterator.next();       // get the value
        }

        //up elevators exsisted
        iterator = _hashUp.iterator();
        ind = rand(0, _hashUp.size());
        for (int i = 0; i < ind; i++)
        {
            iterator.next();
        }
        return (int) iterator.next();
    }

    /**
     * get the random value of the elevator that going up or resting
     * @return
     */
    private int getRandDown()
    {
        int ind = 0;
        Iterator iterator = _hashLevel.iterator();
        if (_hashDown.size() == 0)                    // if there is no down elevator there take from the resting one's
        {
            ind = rand(0, _hashLevel.size());

            for (int i = 0; i < ind; i++)
            {
                iterator.next();
            }
            return (int) iterator.next();
        }

        iterator = _hashDown.iterator();
        ind = rand(0, _hashDown.size());
        for (int i = 0; i < ind; i++)
        {
            iterator.next();
        }
        return (int) iterator.next();
    }

    /**
     * empty the content of the calles that didnt got any assinings
     * @param elev
     */
    private void emptyBuffer(int elev)
    {
        if (_stopLists[elev].size() == 0) // if there are no more stops
        {
            while (_callsBuffer[elev].size() > 0)       // continue to remove from buffer and set to the stops until there is no more calls in buffer
            {
                CallForElevator c = _callsBuffer[elev].remove(0);
                _stopLists[elev].add(c.getSrc());
                _stopLists[elev].add(c.getDest());
            }
        }
    }
}