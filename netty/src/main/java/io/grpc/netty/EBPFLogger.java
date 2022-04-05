package io.grpc.netty;

/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http2.Http2Flags;
import io.netty.handler.codec.http2.Http2FrameLogger;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2Settings;
import io.netty.handler.logging.LogLevel;
import io.netty.util.internal.UnstableApi;
import io.netty.util.internal.logging.InternalLogLevel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import com.vlasrado.sdt.Probe;
import com.vlasrado.sdt.Provider;


import static io.netty.util.internal.ObjectUtil.checkNotNull;

/**
 * Logs HTTP2 frames for debugging purposes.
 */
@UnstableApi
public class EBPFLogger extends Http2FrameLogger {

    public enum Direction {
        INBOUND,
        OUTBOUND
    }

    Provider provider;
    Probe probe;
    private static final int BUFFER_LENGTH_THRESHOLD = 64;

    public EBPFLogger(LogLevel level) {
        this(level, InternalLoggerFactory.getInstance(io.netty.handler.codec.http2.Http2FrameLogger.class));
    }

    public EBPFLogger(LogLevel level, String name) {
        this(level, InternalLoggerFactory.getInstance(checkNotNull(name, "name")));
    }

    public EBPFLogger(LogLevel level, Class<?> clazz) {
        this(level, InternalLoggerFactory.getInstance(checkNotNull(clazz, "clazz")));
    }

    private EBPFLogger(LogLevel level, InternalLogger logger) {
       super(level, logger.toString());
        provider = new Provider("grpcheader");
        probe = provider.addProbe("probe", Probe.STRING);
        provider.load();
    }
    @Override
    public boolean isEnabled() {
        return probe.isEnabled();
    }
    @Override
    public void logData(io.netty.handler.codec.http2.Http2FrameLogger.Direction direction, ChannelHandlerContext ctx, int streamId, ByteBuf data, int padding,
                        boolean endStream) {
        if (isEnabled()) {
            probe.fire("{} {} DATA: streamId={} padding={} endStream={} length={} bytes={}", ctx.channel(),
                    direction.name(), streamId, padding, endStream, data.readableBytes(), toString(data));
        }
    }
    @Override
    public void logHeaders(io.netty.handler.codec.http2.Http2FrameLogger.Direction direction, ChannelHandlerContext ctx, int streamId, Http2Headers headers,
                           int padding, boolean endStream) {
        if (isEnabled()) {
            probe.fire( "{} {} HEADERS: streamId={} headers={} padding={} endStream={}", ctx.channel(),
                    direction.name(), streamId, headers, padding, endStream);
        }
    }
    @Override
    public void logHeaders(io.netty.handler.codec.http2.Http2FrameLogger.Direction direction, ChannelHandlerContext ctx, int streamId, Http2Headers headers,
                           int streamDependency, short weight, boolean exclusive, int padding, boolean endStream) {
        if (isEnabled()) {
            probe.fire("{} {} HEADERS: streamId={} headers={} streamDependency={} weight={} exclusive={} " +
                            "padding={} endStream={}", ctx.channel(),
                    direction.name(), streamId, headers, streamDependency, weight, exclusive, padding, endStream);
        }
    }
    @Override
    public void logPriority(io.netty.handler.codec.http2.Http2FrameLogger.Direction direction, ChannelHandlerContext ctx, int streamId, int streamDependency,
                            short weight, boolean exclusive) {
        if (isEnabled()) {
            probe.fire("{} {} PRIORITY: streamId={} streamDependency={} weight={} exclusive={}", ctx.channel(),
                    direction.name(), streamId, streamDependency, weight, exclusive);
        }
    }
    @Override
    public void logRstStream(io.netty.handler.codec.http2.Http2FrameLogger.Direction direction, ChannelHandlerContext ctx, int streamId, long errorCode) {
        if (isEnabled()) {
            probe.fire( "{} {} RST_STREAM: streamId={} errorCode={}", ctx.channel(),
                    direction.name(), streamId, errorCode);
        }
    }
    @Override
    public void logSettingsAck(io.netty.handler.codec.http2.Http2FrameLogger.Direction direction, ChannelHandlerContext ctx) {
        probe.fire( "{} {} SETTINGS: ack=true", ctx.channel(), direction.name());
    }
    @Override
    public void logSettings(io.netty.handler.codec.http2.Http2FrameLogger.Direction direction, ChannelHandlerContext ctx, Http2Settings settings) {
        if (isEnabled()) {
            probe.fire( "{} {} SETTINGS: ack=false settings={}", ctx.channel(), direction.name(), settings);
        }
    }
    @Override
    public void logPing(io.netty.handler.codec.http2.Http2FrameLogger.Direction direction, ChannelHandlerContext ctx, long data) {
        if (isEnabled()) {
            probe.fire( "{} {} PING: ack=false bytes={}", ctx.channel(),
                    direction.name(), data);
        }
    }
    @Override
    public void logPingAck(io.netty.handler.codec.http2.Http2FrameLogger.Direction direction, ChannelHandlerContext ctx, long data) {
        if (isEnabled()) {
            probe.fire(  "{} {} PING: ack=true bytes={}", ctx.channel(),
                    direction.name(), data);
        }
    }
    @Override
    public void logPushPromise(io.netty.handler.codec.http2.Http2FrameLogger.Direction direction, ChannelHandlerContext ctx, int streamId, int promisedStreamId,
                               Http2Headers headers, int padding) {
        if (isEnabled()) {
            probe.fire( "{} {} PUSH_PROMISE: streamId={} promisedStreamId={} headers={} padding={}",
                    ctx.channel(), direction.name(), streamId, promisedStreamId, headers, padding);
        }
    }
    @Override
    public void logGoAway(io.netty.handler.codec.http2.Http2FrameLogger.Direction direction, ChannelHandlerContext ctx, int lastStreamId, long errorCode,
                          ByteBuf debugData) {
        if (isEnabled()) {
            probe.fire( "{} {} GO_AWAY: lastStreamId={} errorCode={} length={} bytes={}", ctx.channel(),
                    direction.name(), lastStreamId, errorCode, debugData.readableBytes(), toString(debugData));
        }
    }
    @Override
    public void logWindowsUpdate(io.netty.handler.codec.http2.Http2FrameLogger.Direction direction, ChannelHandlerContext ctx, int streamId,
                                 int windowSizeIncrement) {
        if (isEnabled()) {
            probe.fire( "{} {} WINDOW_UPDATE: streamId={} windowSizeIncrement={}", ctx.channel(),
                    direction.name(), streamId, windowSizeIncrement);
        }
    }
    @Override
    public void logUnknownFrame(io.netty.handler.codec.http2.Http2FrameLogger.Direction direction, ChannelHandlerContext ctx, byte frameType, int streamId,
                                Http2Flags flags, ByteBuf data) {
        if (isEnabled()) {
            probe.fire("{} {} UNKNOWN: frameType={} streamId={} flags={} length={} bytes={}", ctx.channel(),
                    direction.name(), frameType & 0xFF, streamId, flags.value(), data.readableBytes(), toString(data));
        }
    }

    private String toString(ByteBuf buf) {
        if (buf.readableBytes() <= BUFFER_LENGTH_THRESHOLD) {
            // Log the entire buffer.
            return ByteBufUtil.hexDump(buf);
        }

        // Otherwise just log the first 64 bytes.
        int length = Math.min(buf.readableBytes(), BUFFER_LENGTH_THRESHOLD);
        return ByteBufUtil.hexDump(buf, buf.readerIndex(), length) + "...";
    }
}

