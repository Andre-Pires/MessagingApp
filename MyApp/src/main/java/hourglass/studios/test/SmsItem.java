package hourglass.studios.test;

/**
 * Created by A. Pires on 17/05/2014.
 */
public class SmsItem {

    private String name;
    private String text;

    public SmsItem(String name, String text) {
        this.name = name;
        this.text = text;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
