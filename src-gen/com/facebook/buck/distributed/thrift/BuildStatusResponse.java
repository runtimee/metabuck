/**
 * Autogenerated by Thrift Compiler (0.9.3)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package com.facebook.buck.distributed.thrift;

import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;

import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.EncodingUtils;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.server.AbstractNonblockingServer.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import javax.annotation.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked"})
@Generated(value = "Autogenerated by Thrift Compiler (0.9.3)", date = "2017-11-16")
public class BuildStatusResponse implements org.apache.thrift.TBase<BuildStatusResponse, BuildStatusResponse._Fields>, java.io.Serializable, Cloneable, Comparable<BuildStatusResponse> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("BuildStatusResponse");

  private static final org.apache.thrift.protocol.TField BUILD_JOB_FIELD_DESC = new org.apache.thrift.protocol.TField("buildJob", org.apache.thrift.protocol.TType.STRUCT, (short)1);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new BuildStatusResponseStandardSchemeFactory());
    schemes.put(TupleScheme.class, new BuildStatusResponseTupleSchemeFactory());
  }

  public BuildJob buildJob; // optional

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    BUILD_JOB((short)1, "buildJob");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // BUILD_JOB
          return BUILD_JOB;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  private static final _Fields optionals[] = {_Fields.BUILD_JOB};
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.BUILD_JOB, new org.apache.thrift.meta_data.FieldMetaData("buildJob", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, BuildJob.class)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(BuildStatusResponse.class, metaDataMap);
  }

  public BuildStatusResponse() {
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public BuildStatusResponse(BuildStatusResponse other) {
    if (other.isSetBuildJob()) {
      this.buildJob = new BuildJob(other.buildJob);
    }
  }

  public BuildStatusResponse deepCopy() {
    return new BuildStatusResponse(this);
  }

  @Override
  public void clear() {
    this.buildJob = null;
  }

  public BuildJob getBuildJob() {
    return this.buildJob;
  }

  public BuildStatusResponse setBuildJob(BuildJob buildJob) {
    this.buildJob = buildJob;
    return this;
  }

  public void unsetBuildJob() {
    this.buildJob = null;
  }

  /** Returns true if field buildJob is set (has been assigned a value) and false otherwise */
  public boolean isSetBuildJob() {
    return this.buildJob != null;
  }

  public void setBuildJobIsSet(boolean value) {
    if (!value) {
      this.buildJob = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case BUILD_JOB:
      if (value == null) {
        unsetBuildJob();
      } else {
        setBuildJob((BuildJob)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case BUILD_JOB:
      return getBuildJob();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case BUILD_JOB:
      return isSetBuildJob();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof BuildStatusResponse)
      return this.equals((BuildStatusResponse)that);
    return false;
  }

  public boolean equals(BuildStatusResponse that) {
    if (that == null)
      return false;

    boolean this_present_buildJob = true && this.isSetBuildJob();
    boolean that_present_buildJob = true && that.isSetBuildJob();
    if (this_present_buildJob || that_present_buildJob) {
      if (!(this_present_buildJob && that_present_buildJob))
        return false;
      if (!this.buildJob.equals(that.buildJob))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    List<Object> list = new ArrayList<Object>();

    boolean present_buildJob = true && (isSetBuildJob());
    list.add(present_buildJob);
    if (present_buildJob)
      list.add(buildJob);

    return list.hashCode();
  }

  @Override
  public int compareTo(BuildStatusResponse other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetBuildJob()).compareTo(other.isSetBuildJob());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetBuildJob()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.buildJob, other.buildJob);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("BuildStatusResponse(");
    boolean first = true;

    if (isSetBuildJob()) {
      sb.append("buildJob:");
      if (this.buildJob == null) {
        sb.append("null");
      } else {
        sb.append(this.buildJob);
      }
      first = false;
    }
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    // check for sub-struct validity
    if (buildJob != null) {
      buildJob.validate();
    }
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    try {
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class BuildStatusResponseStandardSchemeFactory implements SchemeFactory {
    public BuildStatusResponseStandardScheme getScheme() {
      return new BuildStatusResponseStandardScheme();
    }
  }

  private static class BuildStatusResponseStandardScheme extends StandardScheme<BuildStatusResponse> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, BuildStatusResponse struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // BUILD_JOB
            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
              struct.buildJob = new BuildJob();
              struct.buildJob.read(iprot);
              struct.setBuildJobIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, BuildStatusResponse struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.buildJob != null) {
        if (struct.isSetBuildJob()) {
          oprot.writeFieldBegin(BUILD_JOB_FIELD_DESC);
          struct.buildJob.write(oprot);
          oprot.writeFieldEnd();
        }
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class BuildStatusResponseTupleSchemeFactory implements SchemeFactory {
    public BuildStatusResponseTupleScheme getScheme() {
      return new BuildStatusResponseTupleScheme();
    }
  }

  private static class BuildStatusResponseTupleScheme extends TupleScheme<BuildStatusResponse> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, BuildStatusResponse struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      BitSet optionals = new BitSet();
      if (struct.isSetBuildJob()) {
        optionals.set(0);
      }
      oprot.writeBitSet(optionals, 1);
      if (struct.isSetBuildJob()) {
        struct.buildJob.write(oprot);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, BuildStatusResponse struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      BitSet incoming = iprot.readBitSet(1);
      if (incoming.get(0)) {
        struct.buildJob = new BuildJob();
        struct.buildJob.read(iprot);
        struct.setBuildJobIsSet(true);
      }
    }
  }

}

