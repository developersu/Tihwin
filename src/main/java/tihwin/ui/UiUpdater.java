/*
    Copyright 2022 Dmitry Isaenko
     
    This file is part of Tihwin.

    Tihwin is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Tihwin is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Tihwin.  If not, see <https://www.gnu.org/licenses/>.
 */
package tihwin.ui;

import tihwin.AwesomeMediator;

import javax.swing.*;

public class UiUpdater {
    private final JProgressBar progressBar;
    private final JLabel statusLine;
    private volatile float totalFilesSizeInBytes;
    private volatile long bytesDone;

    public UiUpdater(JProgressBar progressBar, JLabel statusLine){
        this.progressBar = progressBar;
        this.statusLine = statusLine;
        progressBar.setMaximum(100);
        progressBar.setStringPainted(true);
    }

    public synchronized void updateProgressBySize(int value){
        bytesDone += value;
        progressBar.setValue((int) (bytesDone / totalFilesSizeInBytes * 100));
    }

    public void setStatus(String status){
        statusLine.setText(status);
    }

    public synchronized void incrementProgressBar(long fileSize){
        this.totalFilesSizeInBytes += fileSize;
    }

    public void close(){
        progressBar.setValue(0);
        progressBar.setStringPainted(false);
        AwesomeMediator.notifyAllConvertsEnded();
    }
}
