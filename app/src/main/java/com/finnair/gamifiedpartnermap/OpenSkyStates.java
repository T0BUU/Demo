package com.finnair.gamifiedpartnermap;

/**
 * Created by noctuaPC on 29.1.2018.
 */

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Collection;

/**
 * Represents states of vehicles at a given time.
 *
 * @author Markus Fuchs, fuchs@opensky-network.org
 */
@JsonDeserialize(using = OpenSkyStatesDeserializer.class)
public class OpenSkyStates {
    private int time;
    private Collection<OpenSkyStateVector> flightStates;

    /**
     * @return The point in time for which states are stored
     */
    public int getTime() {
        return time;
    }
    public void setTime(int time) {
        this.time = time;
    }

    /**
     * @return Actual states for this point in time
     */
    public Collection<OpenSkyStateVector> getStates() {
        if (flightStates == null || flightStates.isEmpty()) return null;
        return this.flightStates;
    }

    public void setStates(Collection<OpenSkyStateVector> states) {
        this.flightStates = states;
    }
}