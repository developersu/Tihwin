/*

     Copyright "2022" Dmitry Isaenko

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
package tihwin;

public class AwesomeMediator {
    private final static AwesomeMediator INSTANCE = new AwesomeMediator();

    private MainAppUi mainAppUi;
    private AwesomeMediator(){}

    public static void setMainUi(MainAppUi ui){
        INSTANCE.mainAppUi = ui;
    }

    public static void notifyAllConvertsEnded(){
        INSTANCE.mainAppUi.notifySplitFinished();
    }
}
