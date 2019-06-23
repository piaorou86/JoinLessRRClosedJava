package com.hatran;

import java.io.*;
import java.util.*;

//读文件
public class ReadTXTOrCSV {
    // 存储TXT文件内容
    private String title;
    private List<SpaceInstance> SpatialDatabase = new ArrayList<SpaceInstance>();
    private double minX = 0;
    private double maxX = 0;
    private double minY = 0;
    private double maxY = 0;

    // 构造函数，参数为要读取的文件在硬盘上的路径，即可以是相对路径也可以是绝对路径（读取TXT文件）
    public ReadTXTOrCSV(String readFilePathName) {
        File file = new File(readFilePathName);
        // InputStream标志那些从不同数据起源产生输入的类（此处由文件数据产生输入）
        try {
            InputStream inputStrean = new FileInputStream(file);
            // InputStreamReader表示字符输入流
            InputStreamReader inputStreamReader = new InputStreamReader(
                    inputStrean);
            // 将文件内容转换成计算机能读懂的数据
            BufferedReader bufferedReader = new BufferedReader(
                    inputStreamReader);
            // 读取文件内容的title（读取文件第1行的内容）
            this.title = bufferedReader.readLine();
            // 按行读取文件中的内容（读取文件第2行的内容）
            String stringLine = bufferedReader.readLine();
            while (stringLine != null) {
                // 记录字符','在字符串stringLine中的位置
                ArrayList<Integer> indexs = new ArrayList<Integer>();
                for (int i = 0; i < stringLine.length(); i++) {
                    if (stringLine.charAt(i) == ',') {
                        indexs.add(i);
                    }
                }

                SpaceInstance space_instance = new SpaceInstance();
                //System.out.println(stringLine);
                space_instance.setInstanceID(Integer.parseInt(stringLine.substring(0, indexs.get(0))));
                space_instance.setFeatureType((char)Integer.parseInt(stringLine.substring(indexs.get(0)+1, indexs.get(1))));
                PositionInSpace position_in_space = new PositionInSpace();
                position_in_space.setInstance(Integer.parseInt(stringLine.substring(indexs.get(0)+1, indexs.get(1))));
                position_in_space.setFeature(Integer.parseInt(stringLine.substring(0, indexs.get(0))));
                position_in_space.setX(Double.parseDouble(stringLine.substring(indexs.get(1)+1, indexs.get(2))));
                position_in_space.setY(Double.parseDouble(stringLine.substring(indexs.get(2)+1, stringLine.length())));

                if(this.SpatialDatabase.size() == 0)
                {
                    this.minX = position_in_space.getX();
                    this.minY = position_in_space.getY();
                }
                if(position_in_space.getX() < minX)
                {
                    minX = position_in_space.getX();
                }
                if(position_in_space.getX() > maxX)
                {
                    maxX = position_in_space.getX();
                }
                if(position_in_space.getY() < minY)
                {
                    minY = position_in_space.getY();
                }
                if(position_in_space.getY() > maxY)
                {
                    maxY = position_in_space.getY();
                }
                space_instance.setPositionInSpace(position_in_space);
                this.SpatialDatabase.add(space_instance);

                stringLine = bufferedReader.readLine();
            }
            // 关闭文件
            bufferedReader.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // 获取表头
    public String getTitle() {
        return this.title;
    }

    // 获取空间数据集
    public Map<String, PositionInSpace> getSpatialDatabase()
    {
        Map<String, PositionInSpace> S = new LinkedHashMap<String, PositionInSpace>(this.SpatialDatabase.size());
        for(int i=0; i<this.SpatialDatabase.size(); i++)
        {
            S.put((int)this.SpatialDatabase.get(i).getFeatureType() + "." + this.SpatialDatabase.get(i).getInstanceID(), this.SpatialDatabase.get(i).getPositionInSpace());
        }

        return S;
    }

    final public double getMinX()
    {
        return this.minX;
    }

    final public double getMaxX()
    {
        return this.maxX;
    }

    final public double getMinY()
    {
        return this.minY;
    }

    final public double getMaxY()
    {
        return this.maxY;
    }

    // 控制台输出所读取的文件的内容
    public void disPlayReadTXTOrCSVContent() {
        System.out.println("从文件中读取的内容(" + this.SpatialDatabase.size() + "条)：");
        for (int i = 0; i < this.SpatialDatabase.size(); i++) {
            this.SpatialDatabase.get(i).DisplaySpaceInstance();
        }
    }
}
