package com.sertook.pairprogramming.models;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.sertook.pairprogramming.files.Utils;

import java.io.Serializable;
import java.util.List;

/**
 * Created by florian on 14/11/2016.
 */
public class ProjectStatus implements Serializable
{
   private static final long serialVersionUID = -4989359995635189903L;
   private List<FileInfos> files;
   private List<String> neededFiles;

   public List<FileInfos> getFiles() {
      return files;
   }

   public void setFiles(List<FileInfos> files) {
      this.files = files;
   }

   public List<String> getNeededFiles() {
      return neededFiles;
   }

   public void setNeededFiles(List<String> neededFiles) {
      this.neededFiles = neededFiles;
   }
}
