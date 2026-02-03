package com.knowledgebase.wrapper.model.common;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Data source configuration for knowledge base initialization.
 */
public class DataSource {
    
    @SerializedName("type")
    private String type;
    
    @SerializedName("info_list")
    private InfoList infoList;
    
    public DataSource() {
    }
    
    public DataSource(String type, InfoList infoList) {
        this.type = type;
        this.infoList = infoList;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public InfoList getInfoList() {
        return infoList;
    }
    
    public void setInfoList(InfoList infoList) {
        this.infoList = infoList;
    }
    
    public static class InfoList {
        @SerializedName("data_source_type")
        private String dataSourceType;
        
        @SerializedName("file_info_list")
        private FileInfoList fileInfoList;
        
        public InfoList() {
        }
        
        public InfoList(String dataSourceType, FileInfoList fileInfoList) {
            this.dataSourceType = dataSourceType;
            this.fileInfoList = fileInfoList;
        }
        
        public String getDataSourceType() {
            return dataSourceType;
        }
        
        public void setDataSourceType(String dataSourceType) {
            this.dataSourceType = dataSourceType;
        }
        
        public FileInfoList getFileInfoList() {
            return fileInfoList;
        }
        
        public void setFileInfoList(FileInfoList fileInfoList) {
            this.fileInfoList = fileInfoList;
        }
    }
    
    public static class FileInfoList {
        @SerializedName("file_ids")
        private List<String> fileIds;
        
        public FileInfoList() {
        }
        
        public FileInfoList(List<String> fileIds) {
            this.fileIds = fileIds;
        }
        
        public List<String> getFileIds() {
            return fileIds;
        }
        
        public void setFileIds(List<String> fileIds) {
            this.fileIds = fileIds;
        }
    }
    
    /**
     * Builder for DataSource with upload_file type.
     */
    public static DataSource forUploadFile(List<String> fileIds) {
        FileInfoList fileInfoList = new FileInfoList(fileIds);
        InfoList infoList = new InfoList("upload_file", fileInfoList);
        return new DataSource("upload_file", infoList);
    }
}
