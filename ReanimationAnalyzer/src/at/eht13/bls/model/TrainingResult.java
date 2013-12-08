package at.eht13.bls.model;

import java.util.Date;

public class TrainingResult {

	private int id;
	private Date date;
	private int duration;
	private int quality;

	public TrainingResult() {
	}

	public TrainingResult(Date date, int duration, int quality) {
		this.date = date;
		this.duration = duration;
		this.quality = quality;
	}

	public TrainingResult(int id, Date date, int duration, int quality) {
		this.id = id;
		this.date = date;
		this.duration = duration;
		this.quality = quality;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public int getQuality() {
		return quality;
	}

	public void setQuality(int quality) {
		this.quality = quality;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

}