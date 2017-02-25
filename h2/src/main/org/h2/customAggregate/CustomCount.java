package org.h2.customAggregate;

import org.h2.api.AggregateFunction;

import java.util.Arrays;

/**
 * Created by yellowflash on 2/15/17.
 */
public class CustomCount implements AggregateFunction {
    int count = 0;


    @Override
    public void init(java.sql.Connection cnctn) throws java.sql.SQLException {
        count = 0;
    }

    @Override
    public int getType(int[] ints) throws java.sql.SQLException {
        if (ints.length != 1) {
            throw new java.sql.SQLException("The aggregate function FIRST must have 1 arguments.");
        }
        return ints[0];
    }

    @Override
    public void add(Object o) throws java.sql.SQLException {
//        Object[] temp = (Object[]) o;
//        System.out.println(Arrays.toString(temp));
        count++;
    }

    @Override
    public Object getResult() throws java.sql.SQLException {
        return count;
    }
}
