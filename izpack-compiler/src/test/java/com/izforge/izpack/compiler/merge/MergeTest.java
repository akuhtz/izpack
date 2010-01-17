package com.izforge.izpack.compiler.merge;

import org.apache.tools.zip.ZipOutputStream;
import org.hamcrest.collection.IsCollectionContaining;
import org.hamcrest.core.Is;
import org.hamcrest.text.StringEndsWith;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test a single file merge
 *
 * @author Anthonin Bonnefoy
 */
public class MergeTest {
    private File zip;

    @Before
    public void setUp() {
        zip = new File("outputZip.zip");
        zip.delete();
    }

    @Test
    public void testMergeSingleFile() throws Exception {
        File file = new File(getClass().getResource("FileMerge.class").getFile());
        assertThat(file.exists(), Is.is(true));
        FileMerge fileMerge = new FileMerge(file);

        doMerge(fileMerge);

        ZipInputStream inputStream = new ZipInputStream(new FileInputStream(zip));
        assertThat(inputStream.available(), Is.is(1));
        ZipEntry zipEntry = inputStream.getNextEntry();
        assertThat(zipEntry.getName(), Is.is("FileMerge.class"));
    }

    @Test
    public void testMergeDirectory() throws Exception {
        File file = new File(getClass().getResource("FileMerge.class").getFile()).getParentFile();
        assertThat(file.exists(), Is.is(true));
        FileMerge fileMerge = new FileMerge(file);

        doMerge(fileMerge);

        ZipInputStream inputStream = new ZipInputStream(new FileInputStream(zip));
        ArrayList<String> arrayList = new ArrayList<String>();
        ZipEntry zipEntry;
        while ((zipEntry = inputStream.getNextEntry()) != null) {
            arrayList.add(zipEntry.getName());
        }
        assertThat(arrayList, IsCollectionContaining.hasItems("FileMerge.class", "MergeManager.class"));
    }

    private void doMerge(Mergeable fileMerge) throws IOException {
        ZipOutputStream outputStream = new ZipOutputStream(zip);
        fileMerge.merge(outputStream);
        outputStream.close();
    }


    @Test
    public void testMergeJarFile() throws Exception {
        Mergeable jarMerge = MergeManager.getMergeableFromPath("junit/framework/Assert.class");
        assertThat(jarMerge, Is.is(JarMerge.class));

        doMerge(jarMerge);

        ZipInputStream inputStream = new ZipInputStream(new FileInputStream(zip));
        assertThat(inputStream.available(), Is.is(1));
        ZipEntry zipEntry = inputStream.getNextEntry();
        assertThat(zipEntry.getName(), Is.is("junit/framework/Assert.class"));
    }

    @Test
    public void testMergePackageFromJar() throws Exception {
        Mergeable jarMerge = MergeManager.getMergeableFromPath("junit/framework/");
        assertThat(jarMerge, Is.is(JarMerge.class));

        doMerge(jarMerge);

        ZipInputStream inputStream = new ZipInputStream(new FileInputStream(zip));
        ZipEntry zipEntry = inputStream.getNextEntry();
        assertThat(zipEntry.getName(), Is.is("junit/framework/Assert.class"));
        zipEntry = inputStream.getNextEntry();
        assertThat(zipEntry.getName(), Is.is("junit/framework/AssertionFailedError.class"));
    }

    @Test
    public void testGetJarPath() throws Exception {
        String jarPath = MergeManager.getJarAbsolutePath("junit/framework/Assert.class");
        assertThat(jarPath, StringEndsWith.endsWith("junit-4.7.jar"));
        assertThat(new File(jarPath).exists(), Is.is(true));
    }

    @Test
    public void testGetJarFromPackage() throws Exception {
        String jarPath = MergeManager.getJarAbsolutePath("junit/framework");
        assertThat(jarPath, StringEndsWith.endsWith("junit-4.7.jar"));
        assertThat(new File(jarPath).exists(), Is.is(true));
    }
}