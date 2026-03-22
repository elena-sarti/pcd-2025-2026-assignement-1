package pcd.sketch03.model;

import java.util.*;

public class CollisionMonitor {

        private int currentIndex = 0;
        private int maxIndex;

        public CollisionMonitor(int numBalls) {
            this.maxIndex = numBalls - 1;
        }

        public synchronized int getIndex() {
            if (currentIndex < maxIndex) {
                return currentIndex++;
            }
            return -1;
        }

        public synchronized void reset(int newMaxIndex) {
            this.maxIndex = newMaxIndex;
            currentIndex = 0;
        }
    }

