import com.example.b2c_core.BuildingType;
import com.example.b2c_core.City;
import com.example.b2c_core.SharedCity;
import com.example.b2c_core.User;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by alex on 4/21/17.
 */
public class GameManagerTests
{
    /**
     * Creates a GameManager and verifies that only the Users are the ones passed in.
     *
     * @throws NoSuchMethodException     for reflection
     * @throws IllegalAccessException    for reflection
     * @throws InstantiationException    for reflection
     * @throws InvocationTargetException for reflection
     */
    @Test
    public void newGameMangerTest() throws NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException
    {
        User u1 = User.exampleUser();
        User u2 = User.exampleUser();
        User u3 = User.exampleUser();
        final ArrayList<User> users = new ArrayList<>();
        users.add(u1);
        users.add(u2);
        users.add(u3);
        GameManager gm = createGame(new UserVerificationServer((User u) ->
        {
            Assert.assertTrue(users.remove(u));
        }), u1, u2, u3);
        System.out.println(gm);
    }

    @Test
    public void gameRunThroughTest() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException
    {
        User u1 = User.exampleUser();
        User u2 = User.exampleUser();
        User u3 = User.exampleUser();
        GameRunThroughTester tester = new GameRunThroughTester();
        GameManager gm = createGame(tester, u1, u2, u3);
        tester.setGameManager(gm);
    }

    private GameManager createGame(IServer dummy, User... users) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException
    {
        Constructor ctor = GameManager.class.getDeclaredConstructor(IServer.class, User[].class);
        ctor.setAccessible(true);
        return (GameManager) ctor.newInstance(dummy, users);
    }

    protected abstract class DummyServer<T> implements IServer
    {
        protected Consumer<T> _callback;

        public DummyServer(Consumer<T> callback)
        {
            _callback = callback;
        }

        @Override
        public abstract void startDraft(User player, BuildingType[] availableTiles, SharedCity leftCity, SharedCity rightCity, SharedCity... otherCities);

        @Override
        public void startPlace(User player, HashMap<User, BuildingType[]> tileToPlace, SharedCity leftCity, SharedCity rightCity, SharedCity... otherCities)
        {

        }
    }

    private class UserVerificationServer extends DummyServer<User>
    {

        public UserVerificationServer(Consumer<User> callback)
        {
            super(callback);
        }

        @Override
        public void startDraft(User player, BuildingType[] availableTiles, SharedCity leftCity, SharedCity rightCity, SharedCity... otherCities)
        {
            _callback.accept(player);
        }
    }

    private class GameRunThroughTester implements IServer
    {
        private GameManager _mngr;
        private HashMap<User, BuildingType[]> _draftResults = new HashMap<>();

        public void setGameManager(GameManager mngr)
        {
            _mngr = mngr;
            returnDraft();
        }

        public void putDraftInfo(User p, BuildingType... tiles)
        {
            _draftResults.put(p, tiles);
        }

        private void returnDraft()
        {
            Iterator<Map.Entry<User, BuildingType[]>> draftData = _draftResults.entrySet().iterator();
            while (draftData.hasNext())
            {
                Map.Entry<User, BuildingType[]> data = draftData.next();

                System.out.println(data.getKey());
                System.out.println(data.getValue()[0]);
                System.out.println(data.getValue()[1]);
                // This must remove things as it goes.
                // Otherwise it gets backed up.
                draftData.remove();
                _mngr.draftResult(data.getKey(), data.getValue());
            }
        }

        @Override
        public void startDraft(User player, BuildingType[] availableTiles, SharedCity leftCity, SharedCity rightCity, SharedCity... otherCities)
        {
            putDraftInfo(player, availableTiles[0], availableTiles[1]);
            if (_mngr != null)
            {
                returnDraft();
            }
        }

        private void placeTile(User player, BuildingType tile, SharedCity city)
        {
            City c = city.getCity();
            for (int y = 0; y < 4; y++)
            {
                for (int x = 0; x < 4; x++)
                {
                    if (_mngr.placeTile(player, tile, city, x, y))
                    {
                        return;
                    }
                }
            }
        }

        @Override
        public void startPlace(User player, HashMap<User, BuildingType[]> tileToPlace, SharedCity leftCity, SharedCity rightCity, SharedCity... otherCities)
        {
            placeTile(player, tileToPlace.get(player)[0], leftCity);
            placeTile(player, tileToPlace.get(player)[1], rightCity);
            _mngr.placeComplete(player);
        }
    }
}
