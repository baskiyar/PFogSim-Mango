package edu.auburn.pFogSim.clustering;

/*******************************************************************************
 * Copyright (c) 2010 Haifeng Li
 *   
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
 *******************************************************************************/

//package smile.clustering.linkage;

/**
 * Ward's linkage. Ward's linkage follows the analysis of variance approach
 * The dissimilarity between two clusters is computed as the
 * increase in the "error sum of squares" (ESS) after fusing two clusters
 * into a single cluster. Ward's Method seeks to choose the successive
 * clustering steps so as to minimize the increase in ESS at each step.
 * Note that it is only valid for Euclidean distance based proximity matrix.
 * 
 * @author Haifeng Li
 */
public class WardLinkage extends Linkage {
    /**
     * The number of samples in each cluster.
     */
    private int[] n;

    /**
     * Constructor.
     * @param proximity  the proximity matrix to store the distance measure of
     * dissimilarity. To save space, we only need the lower half of matrix.
     */
    public WardLinkage(double[][] proximity) {
        this.proximity = proximity;
        n = new int[proximity.length];
        for (int i = 0; i < n.length; i++) {
            n[i] = 1;
            for (int j = 0; j < i; j++)
                proximity[i][j] *= proximity[i][j];
        }
    }

    @Override
    public String toString() {
        return "Ward's linkage";
    }

    @Override
    public void merge(int i, int j) {
        double nij = n[i] + n[j];

        for (int k = 0; k < i; k++) {
            proximity[i][k] = (proximity[i][k] * (n[i] + n[k]) + proximity[j][k] * (n[j] + n[k]) - proximity[j][i] * n[k]) / (nij + n[k]);
        }

        for (int k = i+1; k < j; k++) {
            proximity[k][i] = (proximity[k][i] * (n[i] + n[k]) + proximity[j][k] * (n[j] + n[k]) - proximity[j][i] * n[k]) / (nij + n[k]);
        }

        for (int k = j+1; k < proximity.length; k++) {
            proximity[k][i] = (proximity[k][i] * (n[i] + n[k]) + proximity[k][j] * (n[j] + n[k]) - proximity[j][i] * n[k]) / (nij + n[k]);
        }

        n[i] += n[j];
    }
}