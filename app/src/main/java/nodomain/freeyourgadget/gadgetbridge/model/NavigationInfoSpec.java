/*  Copyright (C) 2016-2020 Andreas Shimokawa

    This file is part of Gadgetbridge.

    Gadgetbridge is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Gadgetbridge is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>. */
package nodomain.freeyourgadget.gadgetbridge.model;

public class NavigationInfoSpec {
    public static final int ACTION_CONTINUE = 1;
    public static final int ACTION_TURN_LEFT = 2;
    public static final int ACTION_TURN_LEFT_SLIGHTLY = 3;
    public static final int ACTION_TURN_LEFT_SHARPLY = 4;
    public static final int ACTION_TURN_RIGHT = 5;
    public static final int ACTION_TURN_RIGHT_SLIGHTLY = 6;
    public static final int ACTION_TURN_RIGHT_SHARPLY = 7;
    public static final int ACTION_KEEP_LEFT = 8;
    public static final int ACTION_KEEP_RIGHT = 9;
    public static final int ACTION_UTURN_LEFT = 10;
    public static final int ACTION_UTURN_RIGHT = 11;
    public static final int ACTION_OFFROUTE = 12;
    public static final int ACTION_ROUNDABOUT_RIGHT = 13;
    public static final int ACTION_ROUNDABOUT_LEFT = 14;

    public String instruction;
    public int distanceToTurn;
    public int nextAction;
}
