import nu.studer.gradle.jooq.JooqGenerate

/*
 * Tencent is pleased to support the open source community by making BK-CI 蓝鲸持续集成平台 available.
 *
 * Copyright (C) 2019 THL A29 Limited, a Tencent company.  All rights reserved.
 *
 * BK-CI 蓝鲸持续集成平台 is licensed under the MIT license.
 *
 * A copy of the MIT License is included in this file.
 *
 *
 * Terms of the MIT License:
 * ---------------------------------------------------
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */


plugins {
    id("nu.studer.jooq")
    kotlin("jvm")
}

val jooqGenerator by project.configurations
val api by project.configurations

dependencies {
    api("org.jooq:jooq")
    jooqGenerator("mysql:mysql-connector-java:8.0.22")
}

val moduleName = name.split("-")[1]
val jooqModules: List<String>? by extra
val moduleNames = if (null == jooqModules || jooqModules!!.isEmpty()) listOf(moduleName) else jooqModules!!
val mysqlPrefix: String? = System.getProperty("mysqlPrefix") ?: System.getenv("mysqlPrefix")

jooq {
    configurations {
        moduleNames.forEach { moduleName ->
            val databaseName = if (mysqlPrefix != null && mysqlPrefix != "") {
                println("jooq build env : $mysqlPrefix")
                mysqlPrefix + moduleName
            } else {
                "${project.extra["DB_PREFIX"]}$moduleName"
            }

            val taskName = if (moduleNames.size == 1) "genenrate" else "${moduleName}Genenrate"

            project.the<SourceSetContainer>()["main"].java {
                srcDir("build/generated-src/jooq/$taskName")
            }

            create(taskName) {
                jooqConfiguration.apply {
                    jdbc.apply {
                        var mysqlURL = System.getProperty("mysqlURL")
                        var mysqlUser = System.getProperty("mysqlUser")
                        var mysqlPasswd = System.getProperty("mysqlPasswd")

                        if (mysqlURL == null) {
                            mysqlURL = System.getenv("mysqlURL")
                            mysqlUser = System.getenv("mysqlUser")
                            mysqlPasswd = System.getenv("mysqlPasswd")
                        }

                        if (mysqlURL == null) {
                            println("use default mysql database.")
                            mysqlURL = project.extra["DB_HOST"]?.toString()
                            mysqlUser = project.extra["DB_USERNAME"]?.toString()
                            mysqlPasswd = project.extra["DB_PASSWORD"]?.toString()
                        }

                        driver = "com.mysql.jdbc.Driver"
                        url = "jdbc:mysql://$mysqlURL/$databaseName?useSSL=false"
                        user = mysqlUser
                        password = mysqlPasswd
                    }
                    generator.apply {
                        name = "org.jooq.codegen.DefaultGenerator"
                        database.apply {
                            name = "org.jooq.meta.mysql.MySQLDatabase"
                            inputSchema = databaseName
                        }

                        strategy.name = "org.jooq.codegen.DefaultGeneratorStrategy"

                        generate.apply {
                            isRelations = false
                            isDeprecated = false
                            isFluentSetters = true
                            isGeneratedAnnotation = false
                            isJavaTimeTypes = true
                        }

                        target.apply {
                            packageName = "com.tencent.devops.model.$moduleName"
                        }
                    }
                }
            }
        }
    }

    tasks.getByName<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>("compileKotlin") {
        destinationDir = File("build/generated-src")
        tasks.matching { it is JooqGenerate }.forEach {
            println("#task: ${it.name} , group: ${it.group}")
            dependsOn(it.name)
        }
    }
}
