package com.showflix.api.admin.domain;

public class DailyNote {
    private String noteDate;   // YYYY-MM-DD
    private String content;
    private String updatedBy;

    public String getNoteDate() { return noteDate; }
    public void setNoteDate(String noteDate) { this.noteDate = noteDate; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
