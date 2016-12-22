/*
 * SonarQube Checkstyle Plugin
 * Copyright (C) 2012 SonarSource
 * sonarqube@googlegroups.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

package org.sonar.plugins.checkstyle.internal;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public final class CheckUtil {
    private CheckUtil() {
    }

    /**
     * Gets the checkstyle's modules. Checkstyle's modules are nonabstract
     * classes from com.puppycrawl.tools.checkstyle package which names end with
     * 'Check', do not contain the word 'Input' (are not input files for UTs),
     * checkstyle's filters and SuppressWarningsHolder class.
     *
     * @return a set of checkstyle's modules names.
     * @throws IOException if the attempt to read class path resources failed.
     */
    public static Set<Class<?>> getCheckstyleModules() throws IOException {
        final Set<Class<?>> checkstyleModules = new HashSet<>();

//        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
//        final ClassPath classpath = ClassPath.from(loader);
//        final String packageName = "com.puppycrawl.tools.checkstyle.checks";
//        final ImmutableSet<ClassPath.ClassInfo> checkstyleClasses = classpath
//                .getTopLevelClassesRecursive(packageName);
//
//        for (ClassPath.ClassInfo clazz : checkstyleClasses) {
//            final Class<?> loadedClass = clazz.load();
//            if (isCheckstyleModule(loadedClass)) {
//                checkstyleModules.add(loadedClass);
//            }
//        }
        return checkstyleModules;
    }

    /**
     * Get's the check's messages.
     * @param module class to examine.
     * @return a set of checkstyle's module message fields.
     * @throws ClassNotFoundException if the attempt to read a protected class fails.
     */
    public static Set<Field> getCheckMessages(Class<?> module) throws ClassNotFoundException {
        final Set<Field> checkstyleMessages = new HashSet<>();

        // get all fields from current class
        final Field[] fields = module.getDeclaredFields();

        for (Field field : fields) {
            if (field.getName().startsWith("MSG_")) {
                checkstyleMessages.add(field);
            }
        }

        // deep scan class through hierarchy
        final Class<?> superModule = module.getSuperclass();

        if (superModule != null) {
            checkstyleMessages.addAll(getCheckMessages(superModule));
        }

        return checkstyleMessages;
    }

    /**
     * Gets the check message 'as is' from appropriate 'messages.properties'
     * file.
     *
     * @param locale the locale to get the message for.
     * @param messageKey the key of message in 'messages*.properties' file.
     * @param arguments the arguments of message in 'messages*.properties' file.
     * @return the check's formatted message.
     */
    public static String getCheckMessage(Class<?> module, String messageKey,
            Object... arguments) {
        final Properties pr = new Properties();
        try {
            pr.load(module.getResourceAsStream("messages.properties"));
        }
        catch (IOException ex) {
            return null;
        }
        final MessageFormat formatter = new MessageFormat(pr.getProperty(messageKey));
        return formatter.format(arguments);
    }
}
