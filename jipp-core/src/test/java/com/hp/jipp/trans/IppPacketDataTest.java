// © Copyright 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.trans;

import com.hp.jipp.encoding.Tag;
import com.hp.jipp.encoding.IppPacket;
import com.hp.jipp.model.Status;
import com.hp.jipp.util.KotlinTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.io.InputStream;

import static com.hp.jipp.encoding.AttributeGroup.groupOf;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class IppPacketDataTest {
    @Mock
    InputStream input;

    @Test
    public void cover() {
        IppPacket packet1 = new IppPacket(Status.successfulOk, 0x123, groupOf(Tag.operationAttributes));
        IppPacketData serverResponse = new IppPacketData(packet1, null);

        KotlinTest.cover(serverResponse,
                new IppPacketData(packet1, null),
                serverResponse.copy(
                        new IppPacket(Status.successfulOk, 0x124, groupOf(Tag.operationAttributes)), null));
    }

    @Test
    public void close() throws IOException {
        IppPacket packet1 = new IppPacket(Status.successfulOk, 0x123, groupOf(Tag.operationAttributes));
        IppPacketData data = new IppPacketData(packet1, input);
        data.close();
        verify(input).close();
    }

    @Test
    public void emptyClose() throws IOException {
        IppPacket packet1 = new IppPacket(Status.successfulOk, 0x123, groupOf(Tag.operationAttributes));
        IppPacketData data = new IppPacketData(packet1, null);
        // No crash
        data.close();
    }
}
