import com.example.b2c_core.BuildingType;
import com.example.b2c_core.SharedCity;
import com.example.b2c_core.User;

import java.util.HashMap;

/**
 * Created by alex on 4/21/17.
 */
public interface IServer
{
    void startDraft(User player, BuildingType[] availableTiles, SharedCity leftCity, SharedCity rightCity, SharedCity... otherCities);
    void startPlace(User player, HashMap<User, BuildingType[]> tileToPlace, SharedCity leftCity, SharedCity rightCity, SharedCity otherCities);
    void boardUpdate(User player, SharedCity updatedCity);
}

/*

    user1 user2 user3

=============[ Drafting Data ]=============
    {
        User currentUser : user2;
        BuildingType tilesToChooseFrom : [];
        SharedCity cityLeft :
        {
            User left  : user1;
            User right : user2; // Me
            City _city;
        }
        SharedCity cityRight :
        {
            User left  : user2; // Me
            User right : user3;
            City _city;
        }
        SharedCity[] otherCities : []
    }

=============[ Placing Data ]=============
    {
        User currentUser : user2;
        Map<User,BuildingTile[]> tiles :
        {
            { user1 : [] },
            { user2 : [] },
            { user3 : [] }
        }
        SharedCity cityLeft :
        {
            User left  : user1;
            User right : user2; // Me
            City _city;
        }
        SharedCity cityRight :
        {
            User left  : user2; // Me
            User right : user3;
            City _city;
        }
        SharedCity[] otherCities : []
    }
 */