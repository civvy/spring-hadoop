/*
 * Copyright 2011 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.hadoop.scripting;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Resource;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.data.hadoop.io.SimplerFileSystem;
import org.springframework.scripting.ScriptSource;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.scripting.support.StaticScriptSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

/**
 * @author Costin Leau
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ScriptingTest {

	@Resource
	private ApplicationContext ctx;

	@Test
	public void testRhinoScript() throws Exception {
		ScriptSource script = new StaticScriptSource("print('Hello, world!')");

		Jsr223ScriptEvaluator eval = new Jsr223ScriptEvaluator();
		eval.setLanguage("javascript");

		System.out.println(eval.evaluate(script));
	}

	@Test
	public void testRhinoHadoopScript() throws Exception {
		ScriptSource script = new ResourceScriptSource(new UrlResource(getClass().getResource("basic-script.js")));

		Jsr223ScriptEvaluator eval = new Jsr223ScriptEvaluator();
		eval.setLanguage("javascript");

		Properties prop = PropertiesLoaderUtils.loadAllProperties("test.properties");

		Configuration config = new Configuration();
		config.set("fs.default.name", prop.getProperty("hd.fs"));

		Map<String, Object> args = new LinkedHashMap<String, Object>();
		FileSystem fs = FileSystem.get(config);
		System.out.println("Fs is " + fs);
		SimplerFileSystem sfs = new SimplerFileSystem(fs);
		System.out.println("Fs is " + sfs);
		
		args.put("fs", sfs);

		eval.evaluate(script, args);
	}

	@Test
	public void testScriptNS() throws Exception {
		assertNotNull(ctx);
		assertTrue(ctx.containsBean("script-js"));
		System.out.println("Script result is " + ctx.getBean("script-js"));
	}
}