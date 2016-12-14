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
import java.util.Properties;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.puppycrawl.tools.checkstyle.api.AutomaticBean;
import com.puppycrawl.tools.checkstyle.api.Filter;

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

        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        final ClassPath classpath = ClassPath.from(loader);
        final String packageName = "com.puppycrawl.tools.checkstyle.checks";
        final ImmutableSet<ClassPath.ClassInfo> checkstyleClasses = classpath
                .getTopLevelClassesRecursive(packageName);

        for (ClassPath.ClassInfo clazz : checkstyleClasses) {
            final Class<?> loadedClass = clazz.load();
            if (isCheckstyleModule(loadedClass)) {
                checkstyleModules.add(loadedClass);
            }
        }
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
     * Checks whether a class may be considered as the checkstyle module.
     * Checkstyle's modules are nonabstract classes which names end with
     * 'Check', do not contain the word 'Input' (are not input files for UTs),
     * checkstyle's filters, checkstyle's file filters and
     * SuppressWarningsHolder class.
     *
     * @param loadedClass class to check.
     * @return true if the class may be considered as the checkstyle module.
     */
    private static boolean isCheckstyleModule(Class<?> loadedClass) {
        final String className = loadedClass.getSimpleName();
        return isCheckstyleNonAbstractCheck(loadedClass, className)
                || isFilterModule(loadedClass, className)
                || "SuppressWarningsHolder".equals(className)
                || "FileContentsHolder".equals(className);
    }

    /**
     * Checks whether a class may be considered as the checkstyle check.
     * Checkstyle's checks are nonabstract classes which names end with 'Check',
     * do not contain the word 'Input' (are not input files for UTs), and extend
     * Check.
     *
     * @param loadedClass class to check.
     * @param className class name to check.
     * @return true if a class may be considered as the checkstyle check.
     */
    private static boolean isCheckstyleNonAbstractCheck(Class<?> loadedClass, String className) {
        return !Modifier.isAbstract(loadedClass.getModifiers()) && className.endsWith("Check")
                && !className.contains("Input");
    }

    /**
     * Checks whether a class may be considered as the checkstyle filter.
     * Checkstyle's filters are classes which are subclasses of AutomaticBean,
     * implement 'Filter' interface, and which names end with 'Filter'.
     *
     * @param loadedClass class to check.
     * @param className class name to check.
     * @return true if a class may be considered as the checkstyle filter.
     */
    private static boolean isFilterModule(Class<?> loadedClass, String className) {
        return Filter.class.isAssignableFrom(loadedClass)
                && AutomaticBean.class.isAssignableFrom(loadedClass)
                && className.endsWith("Filter");
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
