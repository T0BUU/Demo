package com.finnair.gamifiedpartnermap;

/**
 * Created by noctuaPC on 24.2.2018.
 */

/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import com.google.maps.android.clustering.Cluster;

import java.util.Set;

/**
 * Renders clusters.
 */
public interface ClusterRenderer<T extends ClusterMarker> {

    /**
     * Called when the view needs to be updated because new clusters need to be displayed.
     * @param clusters the clusters to be displayed.
     */
    void onClustersChanged(Set<? extends Cluster<T>> clusters);

    void setOnClusterClickListener(com.finnair.gamifiedpartnermap.ClusterManager.OnClusterClickListener<T> listener);

    void setOnClusterInfoWindowClickListener(com.finnair.gamifiedpartnermap.ClusterManager.OnClusterInfoWindowClickListener<T> listener);

    void setOnClusterItemClickListener(com.finnair.gamifiedpartnermap.ClusterManager.OnClusterItemClickListener<T> listener);

    void setOnClusterItemInfoWindowClickListener(com.finnair.gamifiedpartnermap.ClusterManager.OnClusterItemInfoWindowClickListener<T> listener);

    /**
     * Called to set animation on or off
     */
    void setAnimation(boolean animate);

    /**
     * Called when the view is added.
     */
    void onAdd();

    /**
     * Called when the view is removed.
     */
    void onRemove();
}
