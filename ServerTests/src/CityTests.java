import com.example.b2c_core.BuildingType;
import com.example.b2c_core.City;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Created by alex on 4/16/17.
 */
public class CityTests
{
    @Test
    public void recreateTest() throws IOException
    {
        City init = new City();
        init.tryAddTile(BuildingType.Shop, 3, 0);
        init.tryAddTile(BuildingType.Factory, 2, 0);
        init.tryAddTile(BuildingType.House, 2, 1);
        init.tryAddTile(BuildingType.Office, 1, 2);

        ObjectMapper m = new ObjectMapper();
        m.writeValue(new File("temp.txt"), init);

        City recreated = m.readValue(new File("temp.txt"), City.class);

        System.out.println(init);
        System.out.println(recreated);
        System.out.println(init.toString().equals(recreated.toString()));
    }
}
