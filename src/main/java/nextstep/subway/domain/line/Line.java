package nextstep.subway.domain.line;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import nextstep.subway.domain.BaseEntity;
import nextstep.subway.domain.collection.LineStations;
import nextstep.subway.domain.station.Station;
import org.apache.commons.lang3.StringUtils;

@Entity
public class Line extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String name;
    private String color;
    private boolean deleted = false;

    @Embedded
    private LineStations lineStations = new LineStations();

    protected Line() {
    }

    public Line(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public void update(String name, String color) {
        if (StringUtils.isNoneEmpty(name)) {
            this.name = name;
        }
        if (StringUtils.isNoneEmpty(color)) {
            this.color = color;
        }
    }

    public void addLineStation(LineStation lineStation) {
        lineStations.add(lineStation);
        lineStation.setLine(this);
    }

    public void addSection(Station upStation, Station downStation, Long distance) {
        lineStations.addSection(this, upStation, downStation, distance);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public void delete() {
        this.deleted = true;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public LineStations getLineStations() {
        return lineStations;
    }

    public void deleteSection(Station station) {
        lineStations.delete(station);
    }
}
