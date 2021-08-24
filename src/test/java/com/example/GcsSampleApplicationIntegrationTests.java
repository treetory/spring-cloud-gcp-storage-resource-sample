/*
 * Copyright 2017-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.awaitility.Awaitility;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This verifies the sample application for using GCP Storage with Spring Resource abstractions.
 *
 * To run the test, set the gcs-resource-test-bucket property in application.properties to the name
 * of your bucket and run: mvn test -Dit.storage
 *
 * @author Daniel Zou
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = { GcsApplication.class })
public class GcsSampleApplicationIntegrationTests {

	private static final Logger LOG = LoggerFactory.getLogger(GcsSampleApplicationIntegrationTests.class);

	@Autowired
	private Storage storage;

	@Autowired
	private TestRestTemplate testRestTemplate;

	@Value("${gcs-resource-test-bucket}")
	private String bucketName;

	private static String NEXT_PAGE_TOKEN = "ChJteS1maWxlLTAwMDA5OC50eHQ=";

//	@BeforeClass
//	public static void checkToRun() {
//		assumeThat(
//				"Google Cloud Storage Resource integration tests are disabled. "
//						+ "Please use '-Dit.storage=true' to enable them. ",
//				System.getProperty("it.storage"), is("true"));
//	}

//	@Before
//	@After
//	public void cleanupCloudStorage() {
//		Page<Blob> blobs = this.storage.list(this.bucketName);
//		for (Blob blob : blobs.iterateAll()) {
//			blob.delete();
//		}
//	}

//	@Test
//	public void testGcsResourceIsLoaded() {
//		BlobId blobId = BlobId.of(this.bucketName, "my-file.txt");
//		BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("text/plain").build();
//		this.storage.create(blobInfo, "Good Morning!".getBytes(StandardCharsets.UTF_8));
//
//		Awaitility.await().atMost(15, TimeUnit.SECONDS)
//				.untilAsserted(() -> {
//					String result = this.testRestTemplate.getForObject("/", String.class);
//					assertThat(result).isEqualTo("Good Morning!\n");
//				});
//
//		this.testRestTemplate.postForObject("/", "Good Night!", String.class);
//		Awaitility.await().atMost(15, TimeUnit.SECONDS)
//				.untilAsserted(() -> {
//					String result = this.testRestTemplate.getForObject("/", String.class);
//					assertThat(result).isEqualTo("Good Night!\n");
//				});
//	}

	/**
	 *  전체조회 테스트
	 */
	@Test
	public void testGcsResourcesRetrieveAll() {
		Page<Blob> blobs = this.storage.list(this.bucketName);
		List<Blob> blobList = new LinkedList<>();
		for (Blob blob : blobs.iterateAll()) {
			LOG.info("{}",
					String.format("%s -> %s", blob.getBlobId().toGsUtilUri(), blob.getName())
			);
			blobList.add(blob);
		}
		LOG.info("{}", blobList.size());
	}

	/**
	 *  페이지 분할 조회 테스트
	 */
	@Test
	public void testGcsResourcesRetrieveByPaging() {
		Page<Blob> blobs1 = this.storage.list(
				this.bucketName,
				Storage.BlobListOption.pageSize(100),
				Storage.BlobListOption.currentDirectory()
		);
		for (Iterator<Blob> ir = blobs1.getValues().iterator(); ir.hasNext();) {
			Blob blob = ir.next();
			LOG.info("{}", blob.getName());
		}
		NEXT_PAGE_TOKEN = blobs1.getNextPageToken();

//		while(blobs1.hasNextPage()) {
//			blobs1 = this.storage.list(
//					this.bucketName,
//					Storage.BlobListOption.pageSize(100),
//					Storage.BlobListOption.currentDirectory(),
//					Storage.BlobListOption.pageToken(NEXT_PAGE_TOKEN)
//			);
//			for (Iterator<Blob> ir = blobs1.getValues().iterator(); ir.hasNext();) {
//				Blob blob = ir.next();
//				LOG.info("{}", blob.getName());
//			}
//			NEXT_PAGE_TOKEN = blobs1.getNextPageToken();
//		}
	}

	/**
	 * 단건조회 테스트
	 */
	@Test
	public void testGcsResourcesRetrieveOne() {
		Awaitility.await().atMost(15, TimeUnit.SECONDS)
			.untilAsserted(() -> {
				String fileName = /*"my-a-file.txt"*/"clipboardImage_21_0702_113845_579.jpeg";
				BlobId blobId = BlobId.of(this.bucketName, fileName);
				Blob blob = this.storage.get(blobId);
				LOG.info(">>>> {}", blob.getName());
				assertThat(blob.getName()).isEqualTo(fileName);
			});
	}

	/**
	 * 파일생성 테스트
	 */
	@Test
	public void testGcsResourceUpload() {
		BlobId blobId = BlobId.of(this.bucketName, "my-a-file.txt");
		BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("text/plain").build();
		this.storage.create(blobInfo, "Good Morning!".getBytes(StandardCharsets.UTF_8));
		Awaitility.await().atMost(15, TimeUnit.SECONDS)
			.untilAsserted(() -> {
				Blob blob = this.storage.get(blobId);
				assertThat(blob.getName()).isEqualTo("my-a-file.txt");
			});
	}

	/**
	 * Mockup 파일 생성 (10만개)
	 */
	@Test
	public void testGcsResourceUploadMockup() {
		for (int i=0; i<100000; i++) {
			BlobId blobId = BlobId.of(this.bucketName, String.format("my-file-%06d.txt", i));
			BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("text/plain").build();
			this.storage.create(blobInfo, String.format("Good Morning! It's a %06d times.", i).getBytes(StandardCharsets.UTF_8));
		}

		Awaitility.await().atMost(60*5, TimeUnit.SECONDS)
			.untilAsserted(() -> {

				List<Blob> blobList = new LinkedList<>();

				Page<Blob> blobs = this.storage.list(
						this.bucketName,
						Storage.BlobListOption.prefix("my-file-"),
						Storage.BlobListOption.currentDirectory()
						);

				for (Blob blob : blobs.iterateAll()) {
					blobList.add(blob);
				}

				assertThat(blobList.size()).isEqualTo(100000);
			});
	}

	/**
	 * 삭제 테스트
	 */
	@Test
	public void testGcsResourceDelete() {
		BlobId blobId = BlobId.of(this.bucketName, "my-a-file.txt");
		this.storage.delete(blobId);
		Awaitility.await().atMost(15, TimeUnit.SECONDS)
			.untilAsserted(() -> {

				Blob blob = this.storage.get(blobId);

				assertThat(blob).isEqualTo(null);
			});
	}

	@Test
	public void testGcsResourceDeleteWholeMockup() throws ExecutionException, InterruptedException {
		Page<Blob> blobs1 = this.storage.list(
				this.bucketName,
				Storage.BlobListOption.pageSize(100),
				Storage.BlobListOption.currentDirectory()
		);
		Deque<FutureTask> deque = new ArrayDeque<>();
		for (Iterator<Blob> ir = blobs1.getValues().iterator(); ir.hasNext();) {
			Blob blob = ir.next();
			FutureTask ft = new FutureTask(() -> storage.delete(blob.getBlobId()));
			deque.add(ft);
		}
		while(deque.size()>0) {
			deque.pop().run();
			LOG.info("DEQUE SIZE >>> {}", deque.size());
		}
	}

	@Test
	public void testGcsResourceDownloadToLocal() {
		String fileName = "my-a-file.txt";
		BlobId blobId = BlobId.of(this.bucketName, fileName);
		Blob blob = this.storage.get(blobId);

		File f = new File(String.format("./%s", fileName));
		blob.downloadTo(Paths.get(f.toURI()));
	}

}
