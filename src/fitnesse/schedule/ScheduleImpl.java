// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.schedule;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ScheduleImpl implements Schedule, Runnable {
  private long delay;
  private Thread thread;
  private boolean running;
  private List<ScheduleItem> scheduleItems = Collections.synchronizedList(new LinkedList<ScheduleItem>());

  public ScheduleImpl(long delay) {
    this.delay = delay;
  }

  public void add(ScheduleItem item) {
    scheduleItems.add(item);
  }

  public void start() {
    running = true;
    thread = new Thread(this);
    thread.setPriority(Thread.MIN_PRIORITY);
    thread.start();
  }

  public void stop() throws Exception {
    running = false;
    if (thread != null) {
      thread.join();
    }
    thread = null;
  }

  public void run() {
    try {
      while (running) {
        runScheduledItems();
        Thread.sleep(delay);
      }
    }
    catch (Exception e) {
    }
  }

  public void runScheduledItems() throws Exception {
    long time = System.currentTimeMillis();
    synchronized (scheduleItems) {
      for (ScheduleItem item : scheduleItems) {
        runItem(item, time);
      }
    }
  }

  private void runItem(ScheduleItem item, long time) throws Exception {
    try {
      if (item.shouldRun(time))
        item.run(time);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}
