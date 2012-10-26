package org.onexus.collection.store.sql;

import org.onexus.collection.api.utils.FieldLink;

import java.util.*;

public class LinksNetworkComparator implements Comparator<String> {

    private String fromCollection;
    private Map<String, List<FieldLink>> networkLinks;

    public LinksNetworkComparator(Map<String, List<FieldLink>> networkLinks, String fromCollection) {
        this.networkLinks = networkLinks;
        this.fromCollection = fromCollection;
    }

    @Override
    public int compare(String a, String b) {

        int a_b = depend(a, b, 0, new HashSet<String>());
        int b_a = depend(b, a, 0, new HashSet<String>());

        if (a_b != -1 && b_a != -1) {
            if (a_b == 0 && b_a == 0) {

                // Remove FieldLink from B to A from linksA
                List<FieldLink> linksA = networkLinks.get(a);
                for (int i=0; i <linksA.size(); i++) {
                    FieldLink link = linksA.get(i);
                    if (link.getFromCollection().equals(b) && link.getToCollection().equals(a)) {
                        linksA.set(i, null);
                    }
                }
                linksA.removeAll(Collections.singletonList(null));

                // Remove FieldLink from A to B from linksB
                List<FieldLink> linksB = networkLinks.get(b);
                for (int i=0; i <linksB.size(); i++) {
                    FieldLink link = linksB.get(i);
                    if (link.getFromCollection().equals(a) && link.getToCollection().equals(b)) {
                        linksB.set(i, null);
                    }
                }
                linksB.removeAll(Collections.singletonList(null));
                return compare(a, b);
            } else {
                throw new RuntimeException("Mutually dependency on different depth level");
            }
        }

        if (a_b != -1) {
            return 1;
        }

        if (b_a != -1) {
            return -1;
        }

        return 0;
    }

    /*
        Check if collection a depends on b

        Returns -1 if not depends, 0 if there is a direct dependency. Otherwise the depth of the dependency.

     */
    private int depend(String a, String b, int depth, Set<String> visitedNodes) {

        // End of the path, no dependency if we reach the fromCollection.
        if (b.equals(fromCollection)) {
            return -1;
        }


        // Break cycles
        if (visitedNodes.contains(a)) {
            return -1;
        }

        List<FieldLink> links = networkLinks.get(a);

        if (links == null || links.isEmpty()) {
            return -1;
        }

        // Check direct dependency
        for (FieldLink link : links) {
            if (link.getToCollection().equals(a) && link.getFromCollection().equals(b)) {
                return depth;
            }
            if (link.getToCollection().equals(b) && link.getFromCollection().equals(a)) {
                return depth;
            }
        }

        visitedNodes.add(a);

        // Check derived dependencies
        int minDerived = -1;
        for (FieldLink link : links) {
            String nextCollection = (link.getToCollection().equals(a) ? link.getFromCollection() : link.getToCollection());
            int derived = depend(nextCollection, b, depth+1, visitedNodes);
            if (derived != -1 && (minDerived==-1 || derived < minDerived)) {
                minDerived = derived;
            }
        }

        return minDerived;

    }
}
