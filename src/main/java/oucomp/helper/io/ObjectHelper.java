package oucomp.helper.io;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

public class ObjectHelper {

    public static Object convertByteArray(byte[] array) {
        try {
            if (array != null) {
                ByteArrayInputStream instream = new ByteArrayInputStream(array);
                ObjectInputStream in = new ObjectInputStream(instream);
                Object obj = in.readObject();
                return obj;
            }
        } catch (Exception ex) {
        }
        return null;
    }
}
