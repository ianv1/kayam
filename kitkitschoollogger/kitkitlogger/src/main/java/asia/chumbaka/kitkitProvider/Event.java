package asia.chumbaka.kitkitProvider;

/**
 * One row from TABLE_EVENTS. Read-only POJO returned by
 * {@link KitkitDBHandler#getEventsForSession(String)}.
 */
public class Event {
    public String eventId;
    public int eventNumber;
    public long eventDatetime;
    public String sessionId;
    public String username;
    public String subject;
    public String level;
    public int day;
    public int game;
    public int stars;
}
