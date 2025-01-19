{{/*
Expand the name of the chart.
*/}}
{{- define "sbiMQ.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common app container name. 
!! Must have list args on include. (list $ . "string-you-wanna-pass")
*/}}
{{- define "sbiMQ.containerName" }}
  {{- $arg := index . 1 | toString }}
  {{- with index . 0 }}
    {{- printf "%s-%s" .Chart.Name $arg }}
  {{- end }}
{{- end }}

{{/*
Common app service resources name. 
!! Must have list args on include. (list $ . "string-you-wanna-pass")
*/}}
{{- define "sbiMQ.resourceFullname" }}
  {{- $arg := index . 1 | toString }}
  {{- with index . 0 }}
    {{- printf "%s-%s" (include "sbiMQ.fullname" .) $arg | trunc 63 | trimSuffix "-" }}
  {{- end }}
{{- end }}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "sbiMQ.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "sbiMQ.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "sbiMQ.labels" -}}
helm.sh/chart: {{ include "sbiMQ.chart" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "sbiMQ.selectorLabels" -}}
app.kubernetes.io/name: {{ include "sbiMQ.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Selector app unic labels
*/}}
{{- define "sbiMQ.selectorAppLabels" -}}
{{- $arg := index . 1 | toString }}
{{- with index . 0 }}
app: {{ printf "%s" (include "sbiMQ.resourceFullname" (list . $arg)) }}
{{- end }}
{{- end }}

{{/*
Unic selector label for app & service 
!! Must have list args in include. (list . "string-you-wanna-pass")
*/}}
{{- define "sbiMQ.deploymentLabels" }}
{{- $arg := index . 1 | toString }}
{{- with index . 0 }}
{{- include "sbiMQ.selectorLabels" . }}
{{ include "sbiMQ.selectorAppLabels" (list . $arg) }}
{{- end }}
{{- end }}

{{/*
Common app image name for deployments
*/}}
{{- define "sbiMQ.image" -}}
{{- printf "%s:%s" .Values.image.name (.Values.image.tag | default "latest") }}
{{- end }}

{{/*
Common app image pullPolicy for deployments
*/}}
{{- define "sbiMQ.imagePullPolicy" -}}
{{- .Values.image.pullPolicy | default "IfNotPresent" }}
{{- end }}

{{/*
Common app imagePullSecrets for deployments
*/}}
{{- define "sbiMQ.imagePullSecrets" -}}
{{- with .Values.image.imagePullSecrets }}
imagePullSecrets:
{{- toYaml . | nindent 2 }}
{{- end }}
{{- end }}

{{/*
Common pod annotations template
*/}}
{{- define "sbiMQ.podAnnotations" -}}
annotations:
  {{- with (index . 1) }}
    {{- toYaml . | nindent 2 }}
  {{- end }}
{{- end }}
