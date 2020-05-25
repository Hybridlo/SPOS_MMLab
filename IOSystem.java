import java.util.Vector;
import java.util.Random;

class IOSystem {
    int max_schedules;
    int curr_schedules = 0;
    int clockticks;
    int curr_clockticks;
    Vector writingPages = new Vector();

    public IOSystem(int schedules, int clockticks) {
        this.max_schedules = schedules;
        this.clockticks = clockticks;
    }

    boolean scheduleWrite(Page page) {
        if (curr_schedules == max_schedules)
            return false;                       //already scheduled maximum allowed amount
        page.scheduled = 1;
        writingPages.addElement(page);
        curr_schedules++;
        return true;
    }

    void tick() {                           //wsclock invokes this after each pointer move if write was scheduled
        curr_clockticks++;
        if (curr_clockticks == clockticks) {
            curr_clockticks = 0;
            oneWrite();
        }
    }

    void oneWrite() {
        Random random = new Random();
        int index = random.nextInt(curr_schedules);

        Page page = (Page) writingPages.elementAt(index);
        page.scheduled = 0;
        page.M = 0;

        writingPages.removeElement(page);
        curr_schedules--;
    }

    void writeAll() {                   //everything scheduled to write finishes in between processor ticks (in Kernel.step)
        curr_clockticks = 0;
        int scheduled_amount = curr_schedules;      //because curr_schedules will be changing in the loop
        for (int i = 0; i < scheduled_amount; i++) {
            oneWrite();
        }
    }
}