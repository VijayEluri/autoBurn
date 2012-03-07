public class MP3 implements Comparable<MP3> {
	private String path;

	private Boolean used;

	public Boolean getUsed() {
		return used;
	}

	public void setUsed(Boolean used) {
		this.used = used;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	private Integer length;

	public Integer getLength() {
		return length;
	}

	public void setLength(Integer length) {
		this.length = length;
	}

	@Override
	public int compareTo(MP3 o) {
		Integer result = 0;

		if (this.length > o.length) {
			result = -1;
		} else if (this.length == o.length) {
			result = 0;
		} else {
			result = 1;
		}

		return result;
	}

	public MP3(String path, Integer length) {
		super();
		this.path = path;
		this.length = length;
		this.used = false;
	}

}
