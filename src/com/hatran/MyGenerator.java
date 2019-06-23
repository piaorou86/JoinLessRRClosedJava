package com.hatran;

import java.util.ArrayList;
import java.util.List;

public class MyGenerator {

    public static List<List<Integer>> cartesianProductInteger(List<List<Integer>> lists) {
        List<List<Integer>> resultLists = new ArrayList<List<Integer>>();
        if (lists.size() == 0) {
            resultLists.add(new ArrayList<Integer>());
            return resultLists;
        } else {
            List<Integer> firstList = lists.get(0);
            List<List<Integer>> remainingLists = cartesianProductInteger(lists.subList(1, lists.size()));
            for (Integer condition : firstList) {
                for (List<Integer> remainingList : remainingLists) {
                    ArrayList<Integer> resultList = new ArrayList<Integer>();
                    resultList.add(condition);
                    resultList.addAll(remainingList);
                    resultLists.add(resultList);
                }
            }
        }
        return resultLists;
    }


//    public static List<List<Integer>> cartesianProductPositionInSpace(List<List<Integer>> lists) {
//
//      List<List<Integer>> resultLists = new ArrayList<>();
//
//        if (lists.size() == 0) {
//            resultLists.add(new ArrayList<>());
//            return resultLists;
//
//        } else {
//            List<Integer> firstList = lists.get(0);
//            List<List<Integer>> remainingLists = cartesianProductPositionInSpace(lists.subList(1, lists.size()));
//
//            for (Integer condition : firstList) {
//                for (List<Integer> remainingList : remainingLists) {
//                    List<Integer> resultList = new ArrayList<>();
//                    resultList.add(condition);
//                    resultList.addAll(remainingList);
//                    resultLists.add(resultList);
//                }
//            }
//        }
//        return resultLists;
//    }


}
