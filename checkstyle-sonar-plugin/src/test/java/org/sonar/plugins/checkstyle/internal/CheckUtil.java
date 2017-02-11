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
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.AbstractFileSetCheck;
import com.puppycrawl.tools.checkstyle.api.AutomaticBean;
import com.puppycrawl.tools.checkstyle.api.Filter;
import com.puppycrawl.tools.checkstyle.guava.collect.ImmutableSet;
import com.puppycrawl.tools.checkstyle.guava.reflect.ClassPath;
import com.puppycrawl.tools.checkstyle.guava.reflect.ClassPath.ClassInfo;

public final class CheckUtil {
    private CheckUtil() {
    }

    /**
     * Gets all checkstyle's modules.
     * @return the set of checkstyle's module classes.
     * @throws IOException if the attempt to read class path resources failed.
     * @see #isCheckstyleModule(Class)
     */
    public static Set<Class<?>> getCheckstyleModules() throws IOException {
        final Set<Class<?>> checkstyleModules = new HashSet<>();

        final ClassLoader loader = Thread.currentThread()
                .getContextClassLoader();
        final ClassPath classpath = ClassPath.from(loader);
        final String packageName = "com.puppycrawl.tools.checkstyle";
        final ImmutableSet<ClassInfo> checkstyleClasses = classpath
                .getTopLevelClassesRecursive(packageName);

        for (ClassPath.ClassInfo clazz : checkstyleClasses) {
            final Class<?> loadedClass = clazz.load();
            if (isCheckstyleModule(loadedClass)) {
                checkstyleModules.add(loadedClass);
            }
        }
        return checkstyleModules;
    }    /**
     * Checks whether a class may be considered as a checkstyle module. Checkstyle's modules are
     * non-abstract classes, which names do not start with the word 'Input' (are not input files for
     * UTs), and are either checkstyle's checks, file sets, filters, file filters, or root module.
     * @param loadedClass class to check.
     * @return true if the class may be considered as the checkstyle module.
     */
    private static boolean isCheckstyleModule(Class<?> loadedClass) {
        final String className = loadedClass.getSimpleName();
        return isValidCheckstyleClass(loadedClass, className)
            && (isCheckstyleCheck(loadedClass)
                    || isFileSetModule(loadedClass)
                    || isFilterModule(loadedClass));
    }

    /**
     * Checks whether a class extends 'AutomaticBean', is non-abstract, and doesn't start with the
     * word 'Input' (are not input files for UTs).
     * @param loadedClass class to check.
     * @param className class name to check.
     * @return true if a class may be considered a valid production class.
     */
    public static boolean isValidCheckstyleClass(Class<?> loadedClass, String className) {
        return AutomaticBean.class.isAssignableFrom(loadedClass)
                && !Modifier.isAbstract(loadedClass.getModifiers())
                && !className.contains("Input");
    }

    /**
     * Checks whether a class may be considered as the checkstyle check.
     * Checkstyle's checks are classes which implement 'AbstractCheck' interface.
     * @param loadedClass class to check.
     * @return true if a class may be considered as the checkstyle check.
     */
    public static boolean isCheckstyleCheck(Class<?> loadedClass) {
        return AbstractCheck.class.isAssignableFrom(loadedClass);
    }

    /**
     * Checks whether a class may be considered as the checkstyle file set.
     * Checkstyle's file sets are classes which implement 'AbstractFileSetCheck' interface.
     * @param loadedClass class to check.
     * @return true if a class may be considered as the checkstyle file set.
     */
    public static boolean isFileSetModule(Class<?> loadedClass) {
        return AbstractFileSetCheck.class.isAssignableFrom(loadedClass);
    }

    /**
     * Checks whether a class may be considered as the checkstyle filter.
     * Checkstyle's filters are classes which implement 'Filter' interface.
     * @param loadedClass class to check.
     * @return true if a class may be considered as the checkstyle filter.
     */
    public static boolean isFilterModule(Class<?> loadedClass) {
        return Filter.class.isAssignableFrom(loadedClass);
    }

    /**
     * Get's the check's messages.
     * @param module class to examine.
     * @return a set of checkstyle's module message fields.
     */
    public static Set<Field> getCheckMessages(Class<?> module) {
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
     * @param module the module to get the message from.
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
        final MessageFormat formatter =
                new MessageFormat(pr.getProperty(messageKey), Locale.ENGLISH);
        return formatter.format(arguments);
    }
}
