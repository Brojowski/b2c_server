/**
 *
 */
public class User
{
    private String uname;

    private User(String username)
    {
        uname = username;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof String)
        {
            String toCompare = (String)o;
            return uname.compareTo(toCompare) == 0;
        }
        else
        {
            return false;
        }
    }

    @Override
    public int hashCode()
    {
        return uname.hashCode();
    }

    public static User exampleUser()
    {
        return new User("Test");
    }
}
