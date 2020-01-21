package org.lab1505.fileUtils;

import com.opencsv.CSVWriter;
import org.jgrapht.Graph;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @author Jie Wu
 * mail wuj1e@cqu.edu.cn
 * @date 2020/1/12 21:44
 */
public class CsvGraphWriter {

    public static <V, E> void write(Graph<V, E> graph, Class<? extends E> edgeType, OutputStream outputStream) {
        write(graph,edgeType,outputStream,null);
    }

    /**
     * Write a {@link Graph} to a local file. The method utilizes every field of
     * edgeType.
     *
     * @param <V>      type of vertex
     * @param <E>      type of edge
     * @param graph    the graph to write
     * @param edgeType Type of edge, should be the same with E
     * @param file     the local file directory to write the graph
     */
    public static <V, E> void write(Graph<V, E> graph, Class<? extends E> edgeType, File file) {

        try (FileOutputStream fos = new FileOutputStream(file)) {
            write(graph, edgeType, fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static <V, E> void write(Graph<V, E> graph, Class<? extends E> edgeType, File file, Comparator<E> comparator) {

        try (FileOutputStream fos = new FileOutputStream(file)) {
            write(graph, edgeType, fos,comparator);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static <V, E> void write(Graph<V, E> graph, Class<? extends E> edgeType, OutputStream os, Comparator<E> comparator) {
        CSVWriter writer = null;
        writer = new CSVWriter(new OutputStreamWriter(os));

        HashMap<String, Integer> keymap = null;
        List<E> edgeList = new ArrayList<>();
        edgeList.addAll(graph.edgeSet());

        if(comparator!=null){
            edgeList.sort(comparator);
        }

        for (E edge : edgeList) {
            Field[] originalFields = edgeType.getDeclaredFields();
//            if(edgeType.getSuperclass()!=null){
//                Field[] superFields = edgeType.getSuperclass().getDeclaredFields();
//                originalFields = ArrayUtils.addAll(originalFields, superFields);
//            }
            ArrayList<Field> fields = new ArrayList<>();
            for (Field f : originalFields) {
                if (!f.isAccessible()) {
                    f.setAccessible(true);
                }
                WriteIgnore writeIgnore = f.getDeclaredAnnotation(WriteIgnore.class);
                if (writeIgnore == null&&!f.getName().equals("this$0")) {
                    fields.add(f);
                }
            }

            /**
             * write header
             */
            if (keymap == null) {
                keymap = new HashMap<String, Integer>();
                int i = 2;

                for (Field f : fields) {
                    keymap.put(f.getName(), i++);
                }

                keymap.put("source_vertex", 0);
                keymap.put("target_vertex", 1);

                String[] headarr = new String[keymap.size()];
                for (Map.Entry<String, Integer> entry : keymap.entrySet()) {
                    headarr[entry.getValue()] = entry.getKey();
                }

                writer.writeNext(headarr);
            }

            /**
             * write content
             */
            String[] contentarr = new String[keymap.size()];
            V source = graph.getEdgeSource(edge);
            V target = graph.getEdgeTarget(edge);
            contentarr[0] = source.toString();
            contentarr[1] = target.toString();

            for (Field f : fields) {
                String fieldName = f.getName();
                String fieldValue = "N/A";
                try {
                    fieldValue = f.get(edge).toString();
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                int index = keymap.get(fieldName);
                contentarr[index] = fieldValue;
            }

            writer.writeNext(contentarr);
        }

        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static <T> void write(List<T> edges, Class<? extends T> edgeType, OutputStream os, Comparator<T> comparator) {
        CSVWriter writer = null;
        writer = new CSVWriter(new OutputStreamWriter(os));
        HashMap<String, Integer> keymap = null;

        if(comparator!=null){
            edges.sort(comparator);
        }

        for (T edge : edges) {
            Field[] originalFields = edgeType.getDeclaredFields();
//            if(edgeType.getSuperclass()!=null){
//                Field[] superFields = edgeType.getSuperclass().getDeclaredFields();
//                originalFields = ArrayUtils.addAll(originalFields, superFields);
//            }
            ArrayList<Field> fields = new ArrayList<>();
            for (Field f : originalFields) {
                if (!f.isAccessible()) {
                    f.setAccessible(true);
                }
                WriteIgnore writeIgnore = f.getDeclaredAnnotation(WriteIgnore.class);
                if (writeIgnore == null&&!f.getName().equals("this$0")) {
                    fields.add(f);
                }
            }

            /**
             * write header
             */
            if (keymap == null) {
                keymap = new HashMap<String, Integer>();
                int i = 0;

                for (Field f : fields) {
                    keymap.put(f.getName(), i++);
                }

                String[] headarr = new String[keymap.size()];
                for (Map.Entry<String, Integer> entry : keymap.entrySet()) {
                    headarr[entry.getValue()] = entry.getKey();
                }

                writer.writeNext(headarr);
            }

            /**
             * write content
             */
            String[] contentarr = new String[keymap.size()];

            for (Field f : fields) {
                String fieldName = f.getName();
                String fieldValue = "N/A";
                try {
                    fieldValue = f.get(edge).toString();
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                int index = keymap.get(fieldName);
                contentarr[index] = fieldValue;
            }

            writer.writeNext(contentarr);
        }

        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static <T> void write(List<T> edges, Class<? extends T> edgeType, OutputStream os) {
        write(edges,edgeType,os,null);
    }
}
