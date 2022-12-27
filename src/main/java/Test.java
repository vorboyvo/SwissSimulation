import java.time.Duration;
import java.time.Instant;

public class Test {
    public static void main(String[] args) {
        Division main = new Division(
                "Main",
                null,
                16,
                1,
                0,
                1005L
        );
        main.rrRunMatches();
        System.out.println(main);
    }
}
