<template>
  <div class="slider-captcha" :class="{ passed }">
    <div ref="track" class="slider-track">
      <div class="slider-fill" :style="{ width: progress + '%' }"></div>
      <div class="slider-text" v-if="!passed">拖动滑块完成验证</div>
      <div class="slider-text success" v-else>验证通过</div>
      <div
        ref="handle"
        class="slider-handle"
        :style="{ left: `${handleLeft}px` }"
        @mousedown.prevent="onStart"
        @touchstart="onStart"
      >
        <i class="el-icon-d-arrow-right"></i>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SliderCaptcha',
  props: {
    value: {
      type: Boolean,
      default: false
    },
    disabled: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      progress: 0,
      dragging: false,
      passed: false,
      trackWidth: 0,
      handleWidth: 0
    };
  },
  computed: {
    maxOffset() {
      return Math.max(0, this.trackWidth - this.handleWidth);
    },
    handleLeft() {
      if (!this.trackWidth || !this.handleWidth) {
        return 0;
      }
      return (this.progress / 100) * this.maxOffset;
    }
  },
  watch: {
    value(val) {
      if (!val) {
        this.reset();
      } else if (val && !this.passed) {
        this.markPassed();
      }
    }
  },
  methods: {
    updateMetrics() {
      const track = this.$refs.track;
      const handle = this.$refs.handle;
      if (!track || !handle) {
        return;
      }
      const rect = track.getBoundingClientRect();
      this.trackWidth = rect.width;
      this.handleWidth = handle.offsetWidth;
    },
    getClientX(event) {
      if (event.touches && event.touches.length) {
        return event.touches[0].clientX;
      }
      return event.clientX;
    },
    onStart(event) {
      if (this.disabled || this.passed) {
        return;
      }
      this.dragging = true;
      const track = this.$refs.track;
      if (!track) {
        return;
      }
      this.updateMetrics();
      this.trackRect = track.getBoundingClientRect();
      this.startX = this.getClientX(event);
      window.addEventListener('mousemove', this.onMove);
      window.addEventListener('mouseup', this.onEnd);
      window.addEventListener('touchmove', this.onMove, { passive: true });
      window.addEventListener('touchend', this.onEnd);
    },
    onMove(event) {
      if (!this.dragging || this.disabled || this.passed) {
        return;
      }
      const clientX = this.getClientX(event);
      const max = Math.max(1, this.trackRect.width - this.handleWidth);
      const delta = clientX - this.trackRect.left - this.handleWidth / 2;
      const clamped = Math.min(max, Math.max(0, delta));
      const percent = Math.min(100, Math.max(0, (clamped / max) * 100));
      this.progress = percent;
    },
    onEnd() {
      if (!this.dragging) {
        return;
      }
      this.dragging = false;
      if (!this.passed) {
        if (this.progress >= 98) {
          this.markPassed();
          return;
        }
        this.progress = 0;
      }
      window.removeEventListener('mousemove', this.onMove);
      window.removeEventListener('mouseup', this.onEnd);
      window.removeEventListener('touchmove', this.onMove);
      window.removeEventListener('touchend', this.onEnd);
    },
    markPassed() {
      this.passed = true;
      this.progress = 100;
      this.$emit('input', true);
      this.$emit('success');
      this.onEnd();
    },
    reset() {
      this.passed = false;
      this.progress = 0;
    }
  },
  mounted() {
    this.updateMetrics();
    window.addEventListener('resize', this.updateMetrics);
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.updateMetrics);
    this.onEnd();
  }
};
</script>

<style scoped>
.slider-captcha {
  width: 100%;
}

.slider-track {
  position: relative;
  height: 44px;
  border-radius: 24px;
  background: #eef3f1;
  overflow: hidden;
  border: 1px solid rgba(31, 122, 109, 0.2);
  touch-action: none;
}

.slider-fill {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 0;
  background: linear-gradient(135deg, #1f7a6d, #c75a31);
  transition: width 0.12s ease;
}

.slider-text {
  position: relative;
  z-index: 2;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #4a5a55;
  font-size: 14px;
}

.slider-text.success {
  color: #ffffff;
  font-weight: 600;
}

.slider-handle {
  position: absolute;
  top: 4px;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #ffffff;
  color: #1f7a6d;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6px 12px rgba(31, 56, 94, 0.18);
  z-index: 3;
  cursor: pointer;
  transition: left 0.12s ease;
  touch-action: none;
}

.slider-captcha.passed .slider-handle {
  color: #ffffff;
  background: rgba(255, 255, 255, 0.25);
  box-shadow: none;
}
</style>
