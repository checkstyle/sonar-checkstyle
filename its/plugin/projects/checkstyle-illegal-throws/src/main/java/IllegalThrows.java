import java.io.IOException;
import java.io.FileNotFoundException;

public class IllegalThrows {

  public void unchecked() throws RuntimeException { // illegal throws: 'RuntimeException' is illegal by default.
  }

}
