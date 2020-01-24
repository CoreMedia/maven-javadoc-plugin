package org.apache.maven.plugins.javadoc;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

class PackageListUtil
{
    static final String PACKAGE_LIST = "package-list";

    private PackageListUtil()
    {
    }

    static List<String> readPackageList( File packageListDir ) throws IOException
    {
        List<String> packageList;
        File packageListFile = new File( packageListDir, PACKAGE_LIST );
        try ( BufferedReader br = new BufferedReader( new FileReader( packageListFile ) ) )
        {
            packageList = br.lines().filter( PackageListUtil::isPackage ).collect( Collectors.toList() );
        }
        return packageList;
    }

    static void writePackageList( List<String> packageList, File packageListDir ) throws IOException
    {
        //noinspection ResultOfMethodCallIgnored
        packageListDir.mkdirs();
        File packageListFile = new File( packageListDir, PACKAGE_LIST );
        //noinspection ResultOfMethodCallIgnored
        packageListFile.createNewFile();
        try ( PrintWriter br = new PrintWriter( new FileWriter( packageListFile ) ) )
        {
            for ( String packageName : packageList )
            {
                br.println( packageName );
            }
        }
    }

    private static boolean isPackage( String line )
    {
        return !line.trim().isEmpty() && !line.startsWith( "#" );
    }

    static List<String> convertPackageListToSourceFileIncludes( Collection<String> packageNames )
    {
        return packageNames.stream()
            .map( PackageListUtil::convertPackageToSourceFileInclude )
            .collect( Collectors.toList() );
    }

    private static String convertPackageToSourceFileInclude( String line )
    {
        return line.trim().replace( '.', '/' ) + "/*.java";
    }

    static List<String> collectPackageLists( Collection<Path> sourcePaths )
    {
        List<String> allPackages;
        allPackages = sourcePaths.stream()
            .filter( sourcePath -> Files.exists( sourcePath.resolve( "package-list" ) ) )
            .flatMap( sourcePath ->
            {
                try
                {
                    return readPackageList( sourcePath.toFile() ).stream();
                }
                catch ( IOException e )
                {
                    throw new IllegalStateException( "Cannot read package-list file " + sourcePath, e );
                }
            } )
            .collect( Collectors.toList() );
        return allPackages;
    }
}
