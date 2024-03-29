/*
 * Copyright (c) 2006-2018 Makoto Yui
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package libs.btree4j.utils.datetime;

import libs.btree4j.utils.lang.DateTimeFormatter;

public final class StopWatch {

    private final String label;
    private long begin = 0;
    private long end = 0;
    private boolean showInSec = false;

    public StopWatch() {
        this(null, false);
    }

    public StopWatch(String label) {
        this(label, false);
    }

    public StopWatch(String label, boolean showInSec) {
        this.label = label;
        this.showInSec = showInSec;
        start();
    }

    public void setShowInSec(boolean showInSec) {
        this.showInSec = showInSec;
    }

    public void start() {
        begin = System.currentTimeMillis();
    }

    public long stop() {
        end = System.currentTimeMillis();
        return end - begin;
    }

    public void suspend() {
        end = System.currentTimeMillis();
    }

    public void resume() {
        begin += (System.currentTimeMillis() - end);
    }

    public void reset() {
        begin = 0;
        end = 0;
    }

    public long elapsed() {
        if (end != 0) {
            return end - begin;
        } else {
            return System.currentTimeMillis() - begin;
        }
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        if (label != null) {
            buf.append(label + ": ");
        }
        long t = elapsed();
        if (showInSec) {
            buf.append(DateTimeFormatter.formatTimeInSec(t));
            buf.append("sec");
        } else {
            buf.append(DateTimeFormatter.formatTime(t));
        }
        return buf.toString();
    }
}
