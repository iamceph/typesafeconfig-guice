package com.github.racc.tscg;


import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.inject.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.racc.tscg.test.ConstructorInjectedPojo;
import com.github.racc.tscg.test.FieldInjectedPojo;
import com.github.racc.tscg.test.MethodInjectedPojo;
import com.github.racc.tscg.test.NestedPojo;
import com.github.racc.tscg.test.ProvidedPojo;
import com.github.racc.tscg.test.TestPojo;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigMemorySize;

public class TypesafeConfigModuleTest {

	private List<Injector> injectors = new LinkedList<>();

	private Module createTestModule() {

		Module testModule = new AbstractModule() {
			@Override
			protected void configure() {
				bind(ConstructorInjectedPojo.class).asEagerSingleton();
				bind(FieldInjectedPojo.class).asEagerSingleton();
				bind(MethodInjectedPojo.class).asEagerSingleton();
			}

			@Provides
			@Singleton
			ProvidedPojo providePojo(
					@TypesafeConfig("provided.boolean") 	  boolean testBoolean,
					@TypesafeConfig("provided.yesBoolean") 	  boolean testYesBoolean,
					@TypesafeConfig("provided.long") 		  long testLong,
					@TypesafeConfig("provided.byte") 		  byte testByte,
					@TypesafeConfig("provided.int") 		  int testInt,
					@TypesafeConfig("provided.double") 		  double testDouble,
					@TypesafeConfig("provided.float") 		  float testFloat,
					@TypesafeConfig("provided.string") 		  String testString,
					@TypesafeConfig("provided.list.boolean")  List<Boolean> testListOfBoolean,
					@TypesafeConfig("provided.list.integer")  List<Integer> testListOfInteger,
					@TypesafeConfig("provided.list.double")   List<Double> testListOfDouble,
					@TypesafeConfig("provided.list.long") 	  List<Long> testListOfLong,
					@TypesafeConfig("provided.list.string")   List<String> testListOfString,
					@TypesafeConfig("provided.list.duration") List<Duration> testListOfDuration,
					@TypesafeConfig("provided.list.size") 	  List<ConfigMemorySize> testListOfSize,
					@TypesafeConfig("provided.list.nested")   List<NestedPojo> testListOfNested,
					@TypesafeConfig("provided.duration") 	  Duration testDuration,
					@TypesafeConfig("provided.size") 		  ConfigMemorySize testSize,
					@TypesafeConfig("provided.map") 		  Map<String, Integer> testMap,
					@TypesafeConfig("provided.map.intkey") 	  Map<Integer, String> testMapIntkey,
					@TypesafeConfig("provided.nested") 		  NestedPojo testNestedPojo
			) {
				return new ProvidedPojo(testBoolean, testYesBoolean, testLong, testByte, testInt, testDouble, testFloat, testString, testListOfBoolean, testListOfInteger, testListOfDouble, testListOfLong, testListOfString, testListOfDuration, testListOfSize, testListOfNested, testDuration, testSize, testMap, testMapIntkey, testNestedPojo);
			}

		};
		return testModule;
	}
	@Before
	public void setup() {
		Config testConf = ConfigFactory.load("conf/test.conf");


		injectors.add(Guice.createInjector(
			TypesafeConfigModule.fromConfigWithPackage(testConf, "com.github.racc"),
				createTestModule()
		));
		injectors.add(Guice.createInjector(
				TypesafeConfigModule.fromConfigUsingClasspathScanner(testConf, "com.github.racc"),
				createTestModule()
		));
	}

	@Test
	public void canInjectPojoViaConstructor() {
		for (Injector injector : injectors) {
			ConstructorInjectedPojo pojo = injector.getInstance(ConstructorInjectedPojo.class);
			assertPojoIsCorrect(pojo);
		}
	}

	@Test
	public void canInjectPojoViaFields() {
		for (Injector injector : injectors) {
			FieldInjectedPojo pojo = injector.getInstance(FieldInjectedPojo.class);
			assertPojoIsCorrect(pojo);
		}
	}

	@Test
	public void canInjectPojoViaMethods() {
		for (Injector injector : injectors) {
			MethodInjectedPojo pojo = injector.getInstance(MethodInjectedPojo.class);
			assertPojoIsCorrect(pojo);
		}
	}

	@Test
	public void canGetProvidedPojo() {
		for (Injector injector : injectors) {
			ProvidedPojo pojo = injector.getInstance(ProvidedPojo.class);
			assertPojoIsCorrect(pojo);
		}
	}

	private void assertPojoIsCorrect(TestPojo pojo) {
		Assert.assertTrue(pojo.isTestBoolean());
		Assert.assertTrue(pojo.isTestYesBoolean());
		Assert.assertEquals(12345679123l, pojo.getTestLong());
		Assert.assertEquals(1, pojo.getTestInt());
		Assert.assertEquals(123, pojo.getTestByte());
		Assert.assertEquals(2.0, pojo.getTestDouble(), 0.001);
		Assert.assertEquals(2.0f, pojo.getTestFloat(), 0.001);
		Assert.assertEquals("test", pojo.getTestString());
		Assert.assertEquals(Duration.of(10, ChronoUnit.SECONDS), pojo.getTestDuration());
		Assert.assertEquals(ConfigMemorySize.ofBytes(524288), pojo.getTestSize());
		
		NestedPojo nestedListPojo = pojo.getTestListOfNested().get(0);
		Assert.assertEquals(3, nestedListPojo.getNestInt());
		
		Map<String, Integer> testMap = pojo.getTestMap();
		Assert.assertEquals(1, testMap.get("one").intValue());

		Map<Integer, String> testMapIntkey = pojo.getTestMapIntkey();
		Assert.assertEquals("one", testMapIntkey.get("1"));
		
		Assert.assertEquals(Arrays.asList(true, false, true), pojo.getTestListOfBoolean());
		Assert.assertEquals(Arrays.asList(1, 2, 3), pojo.getTestListOfInteger());
		Assert.assertEquals(Arrays.asList(1.1, 2.2, 3.3), pojo.getTestListOfDouble());
		Assert.assertEquals(Arrays.asList(12345679121L, 12345679122L, 12345679123L), pojo.getTestListOfLong());
		Assert.assertEquals(Arrays.asList("a", "b", "c"), pojo.getTestListOfString());
		Assert.assertEquals(Arrays.asList(Duration.of(1, ChronoUnit.SECONDS), Duration.of(2, ChronoUnit.SECONDS), Duration.of(3, ChronoUnit.SECONDS)), pojo.getTestListOfDuration());
		Assert.assertEquals(Arrays.asList(ConfigMemorySize.ofBytes(524288), ConfigMemorySize.ofBytes(1048576), ConfigMemorySize.ofBytes(1073741824)), pojo.getTestListOfSize());
	}
}
