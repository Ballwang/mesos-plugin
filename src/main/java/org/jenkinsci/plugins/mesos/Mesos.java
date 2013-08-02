/*
 * Copyright 2013 Twitter, Inc. and other contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jenkinsci.plugins.mesos;

public abstract class Mesos {
  private static MesosImpl mesos;

  public static class JenkinsSlave {
    String name;

    public JenkinsSlave(String name) {
      this.name = name;
    }
  }

  public static class SlaveRequest {
    JenkinsSlave slave;
    final int cpus;
    final int mem;

    public SlaveRequest(JenkinsSlave slave, int cpus, int mem) {
      this.slave = slave;
      this.cpus = cpus;
      this.mem = mem;
    }
  }

  interface SlaveResult {
    void running(JenkinsSlave slave);

    void finished(JenkinsSlave slave);

    void failed(JenkinsSlave slave);
  }

  abstract public void startScheduler(String jenkinsMaster, String mesosMaster);
  abstract public boolean isSchedulerRunning();
  abstract public void stopScheduler();

  /**
   * Starts a jenkins slave asynchronously in the mesos cluster.
   *
   * @param request
   *          slave request.
   * @param result
   *          this callback will be called when the slave starts.
   */
  abstract public void startJenkinsSlave(SlaveRequest request, SlaveResult result);


  /**
   * Stop a jenkins slave asynchronously in the mesos cluster.
   *
   * @param name
   *          jenkins slave.
   *
   */
  abstract public void stopJenkinsSlave(String name);

  /**
   * @return the mesos implementation instance
   */
  public static synchronized Mesos getInstance() {
    if (mesos == null) {
      mesos = new MesosImpl();
    }
    return mesos;
  }

  public static class MesosImpl extends Mesos {
    @Override
    public synchronized void startScheduler(String jenkinsMaster, String mesosMaster) {
      stopScheduler();
      scheduler = new JenkinsScheduler(jenkinsMaster, mesosMaster);
      scheduler.init();
    }

    @Override
    public synchronized boolean isSchedulerRunning() {
      return scheduler != null && scheduler.isRunning();
    }

    @Override
    public synchronized void stopScheduler() {
      if (scheduler != null) {
        scheduler.stop();
        scheduler = null;
      }
    }

    @Override
    public synchronized void startJenkinsSlave(SlaveRequest request, SlaveResult result) {
      if (scheduler != null) {
        scheduler.requestJenkinsSlave(request, result);
      }
    }

    @Override
    public synchronized void stopJenkinsSlave(String name) {
      if (scheduler != null) {
        scheduler.terminateJenkinsSlave(name);
      }
    }

    private JenkinsScheduler scheduler;
  }
}
