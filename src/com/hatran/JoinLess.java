/**
 * Mining co-location based join less approach
 * Date:
 *
 */

package com.hatran;

import com.rits.cloning.Cloner;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;



public class JoinLess {

    private final static Logger logger = Logger.getLogger(JoinLess.class);  // use log4j to save output message


    /**
     * This program generate the size k candidates
     * @param oldCandidate
     * @param k
     * @return
     */
    public static List<List<Integer>> gen_size_k_candidate(Map<List<Integer>, Float> oldCandidate, Integer k){

        List<List<Integer>> sizeKPlusCandidate = new ArrayList<>();

        // size 2 candidate
        if( k <= 1){
            List<Integer> size1Candidate = new ArrayList<>();
            for (List<Integer> size1: oldCandidate.keySet()){
                size1Candidate.addAll(size1);
            }
            // sort
            Collections.sort(size1Candidate);
            // Generate
            sizeKPlusCandidate = CombinationGenerator.findsort(size1Candidate, 2);

        }else {
            List<List<Integer>> oldList = new ArrayList<>();
            oldList.addAll(oldCandidate.keySet());

            for(int iOld = 0; iOld < oldList.size()-1; iOld ++){
                for(int jOld =iOld+1; jOld < oldList.size(); jOld++){
                    // check k element of feature is the same
                    List<Integer> formerList = new ArrayList<>();
                    List<Integer> laterList = new ArrayList<>();

                    for (int fl=0; fl < k-1; fl++){
                        formerList.add(oldList.get(iOld).get(fl));
                        laterList.add(oldList.get(jOld).get(fl));
                    }

                    if (formerList.equals(laterList)){

                        HashSet<Integer> tempCandidate = new HashSet<>();

                        tempCandidate.addAll(oldList.get(iOld));
                        tempCandidate.addAll(oldList.get(jOld));

                        List<Integer> tempCandidateList = new ArrayList<>();
                        tempCandidateList.addAll(tempCandidate);

                        Collections.sort(tempCandidateList);
                        // Prunning
                        List<List<Integer>> prunningCandidate = CombinationGenerator.findsort(tempCandidateList, k);

                        int flag = 0;
                        for(List<Integer> checkPrun: prunningCandidate){
                            if(oldCandidate.containsKey(checkPrun)){
                                flag++;
                            }
                        }
                        if (flag == k+1){
                            sizeKPlusCandidate.add(tempCandidateList);
                        }
                    }
                }
            }
        }
        return sizeKPlusCandidate;
    }



    public static List<PositionInSpace> CheckFeatureEqual(PositionInSpace Feature1, Integer Feature2){
        List<PositionInSpace> result = new ArrayList<>();
        if (Feature1.getFeature() == Feature2){
            result.add(Feature1);
        }
        return result;
    }


    public static Map<List<Integer>, List<List<Integer>>> CollectTableInstasncesSize2(List<List<Integer>> candidateList,
                                                                                      Map<Integer,Map<List<Integer>, List<List<Integer>>>> starNeighbor){

        // [A, B]:[[1,2],[1,3]]
        Map<List<Integer>, List<List<Integer>>> tableInstances = new HashMap<>();
        Set<List<Integer>> candidateSet = new HashSet<>(candidateList);

        // collect size k=2
        for(List<Integer> candidate: candidateSet) {
            // check if the feature is in the star neighbor
            if (starNeighbor.containsKey(candidate.get(0))) {

                // Only consider this feature
                Map<List<Integer>, List<List<Integer>>> firstValue = starNeighbor.get(candidate.get(0));

                // store the table instances of the current candidate
                List<List<Integer>> currentCandidateTableInstance = new ArrayList<>();

                // package the hashmap to iterator for loop
                Iterator<Map.Entry<List<Integer>, List<List<Integer>>>> itr = firstValue.entrySet().iterator();
                while (itr.hasNext()) {
                    Map.Entry<List<Integer>, List<List<Integer>>> entry = itr.next();

                    List<Integer> tempInner = entry.getValue().parallelStream().
                            filter(point -> point.get(0).equals(candidate.get(1)))
                            .map(st->st.get(1))
                            .collect(Collectors.toList());
//                    List<Integer> tempInner = new ArrayList<>();
//                    entry.getValue().forEach( point->{
//                        if (point.get(0) == candidate.get(1)){
//                            tempInner.add(point.get(1));
//                        }
//                    });

                    if (!tempInner.isEmpty()) {
                        List<Integer> firstFeature = new ArrayList<>(Arrays.asList(entry.getKey().get(1)));
                        List<List<Integer>> firstPoints = new ArrayList<>();
                        firstPoints.add(firstFeature);
                        firstPoints.add(tempInner);
                        currentCandidateTableInstance.addAll(MyGenerator.cartesianProductInteger(firstPoints));
                    }
                }
                if (!currentCandidateTableInstance.isEmpty()) {
                    // put into table instance
                    tableInstances.put(candidate, currentCandidateTableInstance);
                }
            }
        }
        return tableInstances;
    }


    /**
     * Collect table instance of the candidates. In this we check complete a row instance, then next
     * @param candidateList
     * @param starNeighbor
     * @param k
     * @return
     */
    public static Map<List<Integer>, List<List<Integer>>> CollectTableInstasnces(List<List<Integer>> candidateList,
                                                                                 Map<Integer,Map<List<Integer>, List<List<Integer>>>> starNeighbor,
                                                                                 Map<List<Integer>, List<List<Integer>>> tableInstanceSizekSub1,
                                                                                 int k){


        // [A, B]:[[1,2],[1,3]]
        // Return table instances
        Map<List<Integer>, List<List<Integer>>> tableInstances = new HashMap<>();

        // convert candidate to hashset
        Set<List<Integer>> candidateSet = new HashSet<>(candidateList);

        for(List<Integer> candidate: candidateSet){
            if (starNeighbor.containsKey(candidate.get(0))){

                // get remain candidate list
                List<Integer> remainCandidateElement = candidate.subList(1, k+1);

                // store the table instance of the current candidate
                List<List<Integer>> realCliquesAll = new ArrayList<>();

                // Loop hashmap
                Iterator<Map.Entry<List<Integer>, List<List<Integer>>>> itr = starNeighbor.get(candidate.get(0)).entrySet().iterator();
                while (itr.hasNext()){
                    Map.Entry<List<Integer>, List<List<Integer>>> entry = itr.next();
                    // put remain instance
                    List<List<Integer>> tempRemain = new ArrayList<>();

                    for (Integer feature : remainCandidateElement) {
                        List<Integer> remainPoint = entry.getValue().parallelStream().filter( point ->
                                point.get(0).equals(feature))
                                .map(inst -> inst.get(1))
                                .collect(Collectors.toList());

                        if (!remainPoint.isEmpty()) {
                            tempRemain.add(remainPoint);
                        }
                    }

                    if (tempRemain.size() == k){
                        // store the first instance into it
                        tempRemain.add(0, Arrays.asList(entry.getKey().get(1)));

                        // Cartesian product tempRemain based on combinatoricslib lib
                        List<List<Integer>> remainFeatureInstance = MyGenerator.cartesianProductInteger(tempRemain);

                        // Check real cliques
                        List<List<Integer>> realClique = remainFeatureInstance.parallelStream().filter( row -> {

                            return tableInstanceSizekSub1.get(remainCandidateElement).contains(row.subList(1, k+1));

                        }).collect(Collectors.toList());

                        if (!realClique.isEmpty()){
                            realCliquesAll.addAll(realClique);
                        }

                    }
                }
                // put in table instances
                if(!realCliquesAll.isEmpty()){
                    tableInstances.put(candidate, realCliquesAll);
                }
            }

        }
        return tableInstances;
    }



    /**
     * Filter prevalent patterns
     * @param tableInstances
     * @param instanceNumbers
     * @param piThreshold
     * @return
     */

    public static Map<List<Integer>, Float> FilterPrevalentPatterns(Map<List<Integer>, List<List<Integer>>> tableInstances,
                                                                    Map<Integer, Float> instanceNumbers,
                                                                    Float piThreshold) {

        Map<List<Integer>, Float> pis = new HashMap<>(); // store the results

        Iterator<Map.Entry<List<Integer>, List<List<Integer>>>> itr = tableInstances.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<List<Integer>, List<List<Integer>>> entry = itr.next();
            // Store the PI of a pattern
            ArrayList<Float> PR = new ArrayList<>();
            entry.getKey().forEach(feature -> {
                Set<Integer> instances = new HashSet<>(entry.getValue().stream().map(rowInstance -> rowInstance.get(entry.getKey().indexOf(feature))).collect(Collectors.toList()));
                PR.add( instances.size() / instanceNumbers.get(feature));
            });

            if (Collections.min(PR) >= piThreshold) {
                pis.put(entry.getKey(), Collections.min(PR));
            }
        }
        return pis;
    }


    /**
     * Check if arraylist contain another arraylist
     * @param list1
     * @param list2
     * @return
     */
    // If arrrayList2 is contained by arrayList1
    public static boolean ArrayListContainArrayList(List<Integer> list1, List<Integer> list2){
        Set<Integer> hashSet1 = new HashSet<>(list1);
        Set<Integer> hashSet2 = new HashSet<>(list2);

        boolean contain = true;

        for (Integer i: hashSet2){
            if (!hashSet1.contains(i)){
                contain = false;
                break;
            }
        }
        return contain;
    }

    /***
     * check if the list1 contains the list2
     * @param list1
     * @param list2
     * @return
     */

    public static boolean CheckDirectlySuperList(List<Integer> list1, List<Integer> list2){

        Set<Integer> hashSet1 = new HashSet<>(list1);
        Set<Integer> hashSet2 = new HashSet<>(list2);

        boolean contain = false;

        if (list1.size()-list2.size()==1){
            contain = true;
            for (Integer i: hashSet2){
                if (!hashSet1.contains(i)){
                    contain = false;
                    break;
                }
            }
        }
        return contain;
    }



    public static void main(String[] args) {

        long start = System.currentTimeMillis(); // Count run of time

        ObjectMapper mapper = new ObjectMapper(); // use for save file

        // Clone
        Cloner cloner = new Cloner();

        logger.info("This is a join-less mining co-location pattern program");

        // Set the prevalent index
        final float piThreshold = 0.1f;
        final int distanceThreshold = 10;

        // Input data10
        String FileName = "./data/dense_feature/dense_feature_40.csv";

        // Save results
        String result = "./out/dense_feature/dense_feature_40_distance_12_PI01_result.json";  // save the result
        String runTime = "./out/dense_feature/dense_feature_40_distance_12_PI01_runningtimes.json";
        String resultSize2 ="./out/dense_feature/dense_feature_40_distance_12_PI01_result_size2.json"; // save size 2
        String resultSize3 ="./out/dense_feature/dense_feature_40_distance_12_PI01_result_size3.json"; // save size 3


        ReadTXTOrCSV reader = new ReadTXTOrCSV(FileName);
//        logger.info("The input dataset: " + reader);

        // Step 2: Pose grid
        // Store instance into a cell
        Map<List<Integer>, List<PositionInSpace>> gridMap = new HashMap<>();

        Map<Integer, Set<PositionInSpace>> instanceNumber = new HashMap<>(); // Store all instance

        List<Integer> x_max = new ArrayList<>(); // store x axis cell
        List<Integer> y_max = new ArrayList<>();

        reader.getSpatialDatabase().forEach((key, value) -> {

            // put feature into featureList
            if (instanceNumber.isEmpty()) {
                Set<PositionInSpace> emptyInstance = new HashSet<>();
                emptyInstance.add(value);
                instanceNumber.put(value.getFeature(), emptyInstance);

            } else {

                if (instanceNumber.containsKey(value.getFeature())) {
                    Set<PositionInSpace> oldInstance = new HashSet<>(instanceNumber.get(value.getFeature()));
                    oldInstance.add(value);
                    instanceNumber.put(value.getFeature(), oldInstance);
                } else {
                    HashSet<PositionInSpace> newInstance = new HashSet<>();
                    newInstance.add(value);
                    instanceNumber.put(value.getFeature(), newInstance);
                }
            }
            // pose grid
            Integer x = (int) Math.floor(value.getX() / distanceThreshold);
            Integer y = (int) Math.floor(value.getY() / distanceThreshold);
            x_max.add(x);
            y_max.add(y);

            List<Integer> tempKey = new ArrayList<>(Arrays.asList(x, y));
            // check if this point has already existed in grid?
            if (gridMap.containsKey(tempKey)) {
                // this key has already been in grid hashmap
                List<PositionInSpace> oldValue = gridMap.get(tempKey);
                oldValue.add(value);
                gridMap.put(tempKey, oldValue);

            } else {
                // this key has not been in grid hasmap
                List<PositionInSpace> tempValue = new ArrayList<>();
                tempValue.add(value);
                gridMap.put(tempKey, tempValue);
            }
        });
        // Count the instances of the features
        Map<Integer, Float> instanceNumbers = new HashMap<>();
        instanceNumber.forEach((k, v) -> {
            instanceNumbers.put(k, (float) v.size());
        });

        // Size k=1 patterns
        Map<List<Integer>, Float> P1 = new HashMap<>();
        instanceNumbers.forEach((key, value) -> {
            List<Integer> pattern = new ArrayList<>(Arrays.asList(key));
            P1.put(pattern, (float) 1);
        });
//        logger.info("P1" + P1);

        // Step 3: Search the star instance
        // 1. Group 4 cell as a block
        Set<List<List<Integer>>> allBlock = new HashSet<>();
        for (int i = 0; i < Collections.max(x_max) + 1; i++) {
            for (int j = 0; j < Collections.max(y_max) + 1; j++) {

                List<Integer> tempBlock1 = new ArrayList<>(Arrays.asList(i, j));
                List<Integer> tempBlock2 = new ArrayList<>(Arrays.asList(i, j + 1));
                List<Integer> tempBlock3 = new ArrayList<>(Arrays.asList(i + 1, j + 1));
                List<Integer> tempBlock4 = new ArrayList<>(Arrays.asList(i + 1, j));
                List<Integer> tempBlock5 = new ArrayList<>(Arrays.asList(i + 1, j - 1));

                List<Integer> tempBlock6 = new ArrayList<>(Arrays.asList(i, j - 1));
                List<Integer> tempBlock7 = new ArrayList<>(Arrays.asList(i-1, j - 1));
                List<Integer> tempBlock8 = new ArrayList<>(Arrays.asList(i-1, j));
                List<Integer> tempBlock9 = new ArrayList<>(Arrays.asList(i-1, j+1));
                allBlock.add(new ArrayList<>(Arrays.asList(tempBlock1, tempBlock2, tempBlock3, tempBlock4, tempBlock5,
                        tempBlock6, tempBlock7, tempBlock8, tempBlock9)));
            }
        }
        // store all of star neighbor// 2. Loop each block to compute the neighbor pairs
        Map<Integer, Map<List<Integer>, List<List<Integer>>>> starNeighbor = new HashMap<>();

        allBlock.forEach(block -> {
            // collect poits in this block
            if (gridMap.containsKey(block.get(0))) {
                List<PositionInSpace> currentCellPoints = gridMap.get(block.get(0));
                Collections.sort(currentCellPoints, new SortByFeature());
                // Get all points in the other four cells
                List<PositionInSpace> fourCellPoints = new ArrayList<>(currentCellPoints);

                for (int i = 1; i < 9; i++) {
                    if (gridMap.containsKey(block.get(i))) {
                        fourCellPoints.addAll(gridMap.get(block.get(i)));
                    }
                }
                // add all points in the other four cells to the list of points of the first cell
                Collections.sort(fourCellPoints, new SortByFeature());
                // Check points in the current cell has neighbor with other points in five cells
                for (PositionInSpace point : currentCellPoints) {
                    fourCellPoints.remove(point);
                    List<List<Integer>> neighborBlock = fourCellPoints.stream().filter(tempPoint -> {//
                        return  ((point.getFeature() < tempPoint.getFeature())
                                && (Math.sqrt(
                                Math.pow(point.getX() - tempPoint.getX(), 2)
                                        + Math.pow(point.getY() - tempPoint.getY(), 2)))
                                <= distanceThreshold);

                    }).map(pt -> Arrays.asList(pt.getFeature(), pt.getInstance())).collect(Collectors.toList());

//                    logger.info("neibor block:"+ neighborBlock);

                    List<Integer> currentPoint = new ArrayList<>(Arrays.asList(point.getFeature(), point.getInstance()));

                    if (!neighborBlock.isEmpty()) {
                        //put into the star neighbor
                        if (starNeighbor.isEmpty()) {
                            Map<List<Integer>, List<List<Integer>>> emptyNeigbor = new HashMap<>();
                            emptyNeigbor.put(currentPoint, neighborBlock);
                            starNeighbor.put(point.getFeature(), emptyNeigbor);

                        } else {
                            // check if exist
                            if (starNeighbor.containsKey(point.getFeature())){

                                Map<List<Integer>, List<List<Integer>>> oldMap = starNeighbor.get(point.getFeature());
                                if (oldMap.containsKey(currentPoint)) {
                                    List<List<Integer>> old = oldMap.get(currentPoint);
                                    old.addAll(neighborBlock);
                                    oldMap.put(currentPoint, old);

                                } else {
                                    oldMap.put(currentPoint, neighborBlock);
                                }

                                starNeighbor.put(point.getFeature(), oldMap);

                            }else {
                                // build new value
                                Map<List<Integer>, List<List<Integer>>> newMap = new HashMap<>();
                                newMap.put(currentPoint, neighborBlock);
                                starNeighbor.put(point.getFeature(), newMap);
                            }

                        }

                    }

                }
            }
        });

        // Step 4: Generate candidate
        long endStarNeighbor = System.currentTimeMillis();
        logger.info("Time for collecting star neighbor is:" + (endStarNeighbor - start) / 1000.0 + " seconds");

        int k = 1; // size of co-location patterns

        List<List<Integer>> candidateListSize2 = gen_size_k_candidate(P1, k); //return size (k+1) candidate patterns

        // collect size 2 table instances
        Map<List<Integer>, List<List<Integer>>> tableInstancesSize2 = CollectTableInstasncesSize2(candidateListSize2, starNeighbor);
        logger.info("Finish collect size 2 table instances.");


        Map<List<Integer>, Float> pisSize2 = FilterPrevalentPatterns(tableInstancesSize2, instanceNumbers, piThreshold);
        logger.info("Finish filter size 2 patterns.");

        // store all table instance
        Map<List<Integer>, List<List<Integer>>> tableInstancesAll = new HashMap<>();
        tableInstancesAll.putAll(tableInstancesSize2);
        // store all pattern with their pi values
        Map<List<Integer>, Float> pisAll = new HashMap<>();
        pisAll.putAll(pisSize2);


        // Step 6: mining size k>2 co-location patterns
        while(!pisSize2.isEmpty()){
            k++;
            // generate candidate
            List<List<Integer>> candidateSizek = gen_size_k_candidate(pisSize2, k);
            logger.info("Finish generate size " + (k+1) + " candidates." );
            // collect table instance
            Map<List<Integer>, List<List<Integer>>> tableInstancesSizek = CollectTableInstasnces(candidateSizek, starNeighbor, tableInstancesSize2, k);
            logger.info("Finish collect sie "+ (k+1)+" table instances.");
            // put into all table instances
            tableInstancesAll.putAll(tableInstancesSizek);
            tableInstancesSize2 = tableInstancesSizek;

            // Filter prevalent patterns
            Map<List<Integer>, Float> pisSize = FilterPrevalentPatterns(tableInstancesSizek, instanceNumbers, piThreshold);
            logger.info("Finish filter size "+ (k+1) +" patterns.");
            if (!pisSize.isEmpty()){
                pisAll.putAll(pisSize);
            }
            pisSize2 = pisSize;
        }
        logger.info("All patterns: " + pisAll.size());

        // Step 7: Mining closed patterns
        // First: group pattern if the pi value is the same
        Map<Float, List<List<Integer>>> samePIPatterns = new HashMap<>();

        pisAll.forEach((keyPI, valuePI)->{
            if (samePIPatterns.isEmpty()){
                List<List<Integer>> emptyValue = new ArrayList<>();
                emptyValue.add(keyPI);
                samePIPatterns.put(valuePI,emptyValue);

            }else {
                if(samePIPatterns.containsKey(valuePI)){
                    List<List<Integer>> oldValuePattern = new ArrayList<>(samePIPatterns.get(valuePI));
                    oldValuePattern.add(keyPI);
                    samePIPatterns.put(valuePI, oldValuePattern);

                }else {
                    List<List<Integer>> newValue = new ArrayList<>();
                    newValue.add(keyPI);
                    samePIPatterns.put(valuePI, newValue);
                }
            }
        });
//        logger.info("The same PI pattern: " + samePIPatterns);

        // Second: check if a pattern is a supper set of other patterns
        Map<Float, List<List<Integer>>> closedPatterns = new HashMap<>();
        samePIPatterns.forEach((sameKey, sameValue)->{
            Collections.sort(sameValue, new SortBySize());

            List<List<Integer>> copyClosedPatterns = new ArrayList<>(sameValue);

            int sizeOfSameValue = sameValue.size();

            for (int i=0; i< sizeOfSameValue-1; i++){
                for(int j=i+1; j<sizeOfSameValue; j++){

                    if (ArrayListContainArrayList(sameValue.get(i), sameValue.get(j))){
                        copyClosedPatterns.remove(sameValue.get(j));
                    }
                }
            }
            closedPatterns.put(sameKey, copyClosedPatterns);

        });

//        logger.info("The closed patterns are: " + closedPatterns);

        // closed with its pi
        Map<List<Integer>, Float> closedPatternsWithPI = new HashMap<>();
        closedPatterns.forEach((closeKey, closeValue)->{
            closeValue.forEach(innerPattern-> {
                closedPatternsWithPI.put(innerPattern, closeKey);
            });
        });
        // sorted
        // sort the RRClosed by pi
        Map<List<Integer>, Float> sortedClosedPatternsWithPI = closedPatternsWithPI
                .entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2)->e1, LinkedHashMap::new));

//        logger.info("The Closed patterns which sorted by pi: " + sortedClosedPatternsWithPI);

        // Step 8: Mining RRClosed patterns
        // First: Put all closed pattern together
        List<List<Integer>> allClosedPatterns = new ArrayList<>();
        closedPatterns.forEach((closedKey, closedValue)->{
            allClosedPatterns.addAll(closedValue);
        });
        // Sort
        Collections.sort(allClosedPatterns, new SortBySizeDescending());

        // Second: Check the closed super patterns set:
        Map<List<Integer>, Float> rrClosedPatterns = new HashMap<>(); // store the final results

        List<List<Integer>> allClosedPatternsClone = cloner.deepClone(allClosedPatterns);

        allClosedPatterns.forEach(pattern ->{
            // check directly superset
            allClosedPatternsClone.remove(pattern);
            List<List<Integer>> superClosed = allClosedPatternsClone.stream().filter(checkPattern->
                    CheckDirectlySuperList(checkPattern, pattern)
            ).collect(Collectors.toList());
            if (superClosed.isEmpty()) {
                // this is a hard pattern
                rrClosedPatterns.put(pattern, pisAll.get(pattern));
            }else {
                // compute the ESD according equal 5
                List<Float> ESD = new ArrayList<>(); //Store the ESD of a pattern

                for (Integer feature: pattern){
                    // get instances of the feature in table instances
                    Set<Integer> instances = new HashSet<>(
                            tableInstancesAll.get(pattern).stream().map(rowInstance ->
                                    rowInstance.get(pattern.indexOf(feature))).collect(Collectors.toList()));

                    // get instances of the super set
                    Set<Integer> combineNumberInstanceSuperSet = new HashSet<>();

                    for(List<Integer> superSetOfCheckPattern: superClosed){
                        int indexOfFeatureinSuperSet = superSetOfCheckPattern.indexOf(feature);

                        combineNumberInstanceSuperSet.addAll(
                                tableInstancesAll.get(superSetOfCheckPattern).stream().map(rowIns ->
                                        rowIns.get(indexOfFeatureinSuperSet))
                                        .collect(Collectors.toList()));
                    }
                    ESD.add(1f - combineNumberInstanceSuperSet.size()/instances.size());
                }

                if (Collections.min(ESD) != 0){
                    // save this pattern
                    rrClosedPatterns.put(pattern, pisAll.get(pattern));
                }
            }
        });

        // sort the RRClosed by pi
        Map<List<Integer>, Float> sortedPIRRClosed = rrClosedPatterns
                .entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2)->e1, LinkedHashMap::new));

//        logger.info("The RRClosed patterns which sorted by pi: " + sortedPIRRClosed);
        logger.info("Final number of patterns is: " + sortedPIRRClosed.size());

        //Save the final result
        try {
            mapper.writeValue(new File(result), sortedPIRRClosed);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Filter maximal size pattern
        List<Integer> allSize = sortedPIRRClosed.entrySet().stream().map(pattern->pattern.getKey().size()).collect(Collectors.toList());
        Set<Integer> allSizeSet = new HashSet<>(allSize);
        logger.info("All size patterns: "+ allSizeSet);
        // Count each size of patterns
        Map<Integer, Integer> countSizek = new HashMap<>();
        allSizeSet.forEach(sizeK -> {
            Map<List<Integer>, Float> tempSizek = sortedPIRRClosed.entrySet()
                    .stream()
                    .filter(patt->patt.getKey().size()==sizeK)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2)->e1, LinkedHashMap::new));
            countSizek.put(sizeK, tempSizek.size());
        });
        logger.info("The number of each size patterns is: " + countSizek);

        // Statistic the size number patterns
        // Size 2
        Map<List<Integer>, Float> size2 = sortedPIRRClosed.entrySet().stream().
                filter( pattern -> pattern.getKey().size()==2 )
                .collect(Collectors.toMap(pt -> pt.getKey(),pt-> pt.getValue()));
        // Sorted size 3 patterns as pi
        Map<List<Integer>, Float> sortedSize2 = size2
                .entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2)->e1, LinkedHashMap::new));

        logger.info("Number of size 2: " + sortedSize2.size());
        logger.info(sortedSize2);

        // Size 3
        Map<List<Integer>, Float> size3 = sortedPIRRClosed.entrySet().stream().
                filter( pattern -> pattern.getKey().size() == 3)
                .collect(Collectors.toMap(pt -> pt.getKey(),pt-> pt.getValue()));
        // Sorted size 3 patterns as pi
        Map<List<Integer>, Float> sortedSize3 = size3
                .entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2)->e1, LinkedHashMap::new));
        logger.info("Number of size 3: " + sortedSize3.size());
        logger.info(sortedSize3);

        // save to json file
        try {
            mapper.writeValue(new File(resultSize2), sortedSize2);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            mapper.writeValue(new File(resultSize3), sortedSize3);
        } catch (IOException e) {
            e.printStackTrace();
        }


        logger.info("Program end!");
        double totalTime = (System.currentTimeMillis()-start)/1000.0;
        logger.info("Total time：" + totalTime + " seconds");

        // Save the total running times
        try {
            mapper.writeValue(new File(runTime), totalTime);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

