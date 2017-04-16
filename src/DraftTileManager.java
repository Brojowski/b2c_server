import com.example.b2c_core.BuildingType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;


/**
 * Created by alex on 4/16/17.
 */
public class DraftTileManager
{
    private final ArrayList<BuildingType> _tiles = new ArrayList<>();
    private int _numTiles;

    public DraftTileManager()
    {
        addTile(BuildingType.Factory ,40);
        addTile(BuildingType.House ,40);
        addTile(BuildingType.Office ,40);
        addTile(BuildingType.Park ,40);
        addTile(BuildingType.Shop ,40);
        addTile(BuildingType.Tavern_Bed ,10);
        addTile(BuildingType.Tavern_Drink ,10);
        addTile(BuildingType.Tavern_Food ,10);
        addTile(BuildingType.Tavern_Music ,10);
        Collections.shuffle(_tiles);
    }

    private void addTile(BuildingType type, int number)
    {
        for (int i = 0; i < number; i++)
        {
            _tiles.add(type);
            _numTiles++;
        }
    }

    private BuildingType drawTile()
    {
        Random rnd = new Random(System.currentTimeMillis());
        int index = rnd.nextInt(_numTiles);
        _numTiles--;
        return _tiles.remove(index);
    }

    public BuildingType[] draft7()
    {
        BuildingType[] tiles = new BuildingType[7];
        for (int i = 0; i < 7; i++)
        {
            tiles[i] = drawTile();
        }
        return tiles;
    }
}
