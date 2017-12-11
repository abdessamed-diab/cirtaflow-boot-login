/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dz.cirtaflow.models.act;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author diab
 */
@Entity
@Table(name = "ACT_RE_DEPLOYMENT", catalog = "dev", schema = "")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ActReDeployment.findAll", query = "SELECT a FROM ActReDeployment a")})
public class ActReDeployment implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "ID_", nullable = false, length = 64)
    private String id;
    @Column(name = "NAME_", length = 255)
    private String name;
    @Column(name = "CATEGORY_", length = 255)
    private String category;
    @Column(name = "KEY_", length = 255)
    private String key;
    @Column(name = "TENANT_ID_", length = 255)
    private String tenantId;
    @Column(name = "DEPLOY_TIME_")
    @Temporal(TemporalType.TIMESTAMP)
    private Date deployTime;
    @Column(name = "ENGINE_VERSION_", length = 255)
    private String engineVersion;
    @OneToMany(mappedBy = "deploymentId")
    private List<ActGeBytearray> actGeBytearrayList;
    @OneToMany(mappedBy = "deploymentId")
    private List<ActReModel> actReModelList;

    public ActReDeployment() {
    }

    public ActReDeployment(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public Date getDeployTime() {
        return deployTime;
    }

    public void setDeployTime(Date deployTime) {
        this.deployTime = deployTime;
    }

    public String getEngineVersion() {
        return engineVersion;
    }

    public void setEngineVersion(String engineVersion) {
        this.engineVersion = engineVersion;
    }

    @XmlTransient
    public List<ActGeBytearray> getActGeBytearrayList() {
        return actGeBytearrayList;
    }

    public void setActGeBytearrayList(List<ActGeBytearray> actGeBytearrayList) {
        this.actGeBytearrayList = actGeBytearrayList;
    }

    @XmlTransient
    public List<ActReModel> getActReModelList() {
        return actReModelList;
    }

    public void setActReModelList(List<ActReModel> actReModelList) {
        this.actReModelList = actReModelList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ActReDeployment)) {
            return false;
        }
        ActReDeployment other = (ActReDeployment) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "cirtaflow.business.cirtaflow.ActReDeployment[ id=" + id + " ]";
    }
    
}
