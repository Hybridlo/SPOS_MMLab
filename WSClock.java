import java.util.Vector;

public class WSClock {
    private int pointer = 0;
    private Vector indexes = new Vector();

    public void init(Vector mem) {
        if (indexes.size() == 0) {                          //init indexes if they weren't before;
            for (int i = 0; i < mem.size(); i++) {          //can't use constructor because replacePage is static
                Page page = ( Page ) mem.elementAt( i );

                if (page.physical == -1)
                    continue;

                indexes.addElement(i);
            }
        }
    }

    public int getReplacable(Vector mem, int tau, int addingPage, IOSystem io) {

        int removingPage = -1;
        int lastUnmodifiedPage = -1;
        int index = -1;
        int moves = 0;                          //to know when full revolution happened
        boolean writeScheduled = false;

        while (moves < indexes.size() || writeScheduled) {      //stop cycle if revolution happened or continue further if write is scheduled
            moves++;
            if (pointer == indexes.size())
                pointer = 0;

            if (writeScheduled) io.tick();

            index = (int) indexes.elementAt(pointer);
            Page page = ( Page ) mem.elementAt( index );

            if (page.R == 1) {                  //if page has R bit set, skip
                page.R = 0;
                pointer++;                      //pointer has to move on cycle end, not begining
                continue;
            }

            if (page.M == 0)
                lastUnmodifiedPage = index;     //in case we don't find non-WS unmodified page
            //last unmodified page will be used if no other page suits our needs

            if (page.lastTouchTime < tau) {     //if page was touched in time less than tau, skip
                pointer++;                      //pointer has to move on cycle end, not begining
                continue;
            }

            if (page.M == 1 && page.scheduled != 1) {                  //if page was modified, skip but schedule write on disk
                writeScheduled = io.scheduleWrite(page);
                pointer++;                      //pointer has to move on cycle end, not begining
                continue;
            }

            removingPage = index;               //pointer points to found page

            break;                              //if no skip conditions met, remap found page
        }

        if (removingPage == -1)
            removingPage = lastUnmodifiedPage;  //use last known unmodified page

        if (removingPage == -1)                 //if all pages are modified, wriet on disk and use last checked
            removingPage = index;

        System.out.println(removingPage);

        indexes.setElementAt(addingPage, indexes.indexOf(removingPage));

        return removingPage;
    }
}