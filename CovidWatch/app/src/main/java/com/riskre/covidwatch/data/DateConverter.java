package com.riskre.covidwatch.data;
//  Created by Zsombor SZABO on 17/03/2020.
//  Copyright © IZE. All rights reserved.
//  See LICENSE.txt for licensing information.
//  

import androidx.room.TypeConverter;

import java.util.Date;

public class DateConverter {

    @TypeConverter
    public static Date toDate(Long dateLong){
        return dateLong == null ? null: new Date(dateLong);
    }

    @TypeConverter
    public static Long fromDate(Date date){
        return date == null ? null : date.getTime();
    }
}
