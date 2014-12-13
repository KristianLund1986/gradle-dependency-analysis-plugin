package com.github.nullstress.asm;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.commons.io.FileUtils;
import org.objectweb.asm.ClassReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * Created with IntelliJ IDEA.
 * User: Kristian
 * Date: 13.07.14
 * Time: 11:57
 */
public class SourceSetScanner {

    public Set<String> analyzeJar(URL url) {
        Set<String> dependencies = new HashSet<String>();
        try {
            JarInputStream in = new JarInputStream(url.openStream());
            JarEntry entry;

            while (( entry = in.getNextJarEntry()) != null ) {
                String name = entry.getName();

                if (name.endsWith( ".class" )){
                    dependencies.add(name.replaceAll("/", "."));
                }
            }
            in.close();
        } catch (IOException e){
            e.printStackTrace();
        }
        return dependencies;
    }

    public Set<String> analyze(URI uri) {
        Set<String> dependencies = new HashSet<String>();
        try {
            File startDir = new File(uri);
            if(!startDir.isDirectory()) {
                return dependencies;
            }
            Collection<File> files = FileUtils.listFiles(startDir, new String[]{"class"}, true);
            for(File file : files) {
                dependencies.addAll(scanFile(FileUtils.openInputStream(file)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dependencies;
    }

    protected Set<String> scanFile(InputStream inputStream){
        Set<String> dependencies = new HashSet<String>();
        try {
            ClassReader reader = new ClassReader(inputStream);
            ASMDependencyAnalyzer visitor = new ASMDependencyAnalyzer();

            reader.accept( visitor, 0 );
            dependencies.addAll( visitor.getClasses() );
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
        catch (IndexOutOfBoundsException e) {
            // some bug inside ASM causes an IOB exception. Log it and move on?
            // this happens when the class isn't valid.
//            System.out.println( "Unable to process: stream" );
        }
        return dependencies;
    }




}
